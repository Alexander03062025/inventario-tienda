"use strict";

/*
 * Frontend en JavaScript puro. Habla con la API REST de Spring Boot
 * usando fetch(). No usa ninguna librería externa.
 */

const API = "/api";

// ---------- utilidades ----------

const $ = (sel) => document.querySelector(sel);
const dinero = (n) => "$" + Number(n).toFixed(2);
const fechaCorta = (iso) => new Date(iso).toLocaleString("es-EC", { dateStyle: "short", timeStyle: "short" });

let toastTimer;
function toast(mensaje, esError = false) {
    const t = $("#toast");
    t.textContent = mensaje;
    t.classList.toggle("toast--error", esError);
    t.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => (t.hidden = true), 3000);
}

/** Envuelve fetch: lanza Error con el mensaje del backend si la respuesta falla. */
async function api(ruta, opciones = {}) {
    const res = await fetch(API + ruta, {
        headers: { "Content-Type": "application/json" },
        ...opciones,
    });
    if (res.status === 204) return null;
    const cuerpo = await res.json().catch(() => ({}));
    if (!res.ok) {
        throw new Error(cuerpo.detail || cuerpo.message || "Error " + res.status);
    }
    return cuerpo;
}

// ---------- estado ----------

let productosCache = [];

// ---------- carga de datos ----------

async function cargarResumen() {
    const r = await api("/resumen");
    $("#kpiProductos").textContent = r.totalProductos;
    $("#kpiStockBajo").textContent = r.productosStockBajo;
    $("#kpiUnidades").textContent = r.unidadesTotales;
    $("#kpiValor").textContent = dinero(r.valorTotalInventario);
}

async function cargarProductos() {
    const buscar = $("#buscador").value.trim();
    const query = buscar ? "?buscar=" + encodeURIComponent(buscar) : "";
    productosCache = await api("/productos" + query);

    const tbody = $("#tbodyProductos");
    tbody.innerHTML = "";
    if (productosCache.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:#6b7683">Sin productos</td></tr>`;
    }
    for (const p of productosCache) {
        const tr = document.createElement("tr");
        if (p.stockBajo) tr.className = "fila-alerta";
        tr.innerHTML = `
            <td>${escapar(p.nombre)}</td>
            <td>${escapar(p.categoria || "—")}</td>
            <td class="num">${dinero(p.precio)}</td>
            <td class="num">${p.stock}${p.stockBajo ? ' <span class="badge badge--alerta">bajo</span>' : ""}</td>
            <td class="num">${p.stockMinimo}</td>
            <td class="num">${dinero(p.valorEnInventario)}</td>
            <td>
                <div class="acciones">
                    <button class="btn btn--icon" data-mov="${p.id}">± Stock</button>
                    <button class="btn btn--icon" data-editar="${p.id}">Editar</button>
                    <button class="btn btn--icon btn--danger" data-borrar="${p.id}">Borrar</button>
                </div>
            </td>`;
        tbody.appendChild(tr);
    }
    actualizarDatalistCategorias();
}

async function cargarStockBajo() {
    const lista = await api("/productos/stock-bajo");
    const tbody = $("#tbodyStockBajo");
    tbody.innerHTML = lista.length
        ? ""
        : `<tr><td colspan="5" style="text-align:center;color:#6b7683">Todo el stock está por encima del mínimo 👍</td></tr>`;
    for (const p of lista) {
        const tr = document.createElement("tr");
        tr.className = "fila-alerta";
        tr.innerHTML = `
            <td>${escapar(p.nombre)}</td>
            <td>${escapar(p.categoria || "—")}</td>
            <td class="num">${p.stock}</td>
            <td class="num">${p.stockMinimo}</td>
            <td><button class="btn btn--icon" data-mov="${p.id}">Reponer</button></td>`;
        tbody.appendChild(tr);
    }
}

async function cargarMovimientos() {
    const lista = await api("/movimientos");
    const tbody = $("#tbodyMovimientos");
    tbody.innerHTML = lista.length
        ? ""
        : `<tr><td colspan="6" style="text-align:center;color:#6b7683">Sin movimientos</td></tr>`;
    for (const m of lista) {
        const clase = m.tipo === "ENTRADA" ? "badge--entrada" : "badge--salida";
        const signo = m.tipo === "ENTRADA" ? "+" : "−";
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${fechaCorta(m.fecha)}</td>
            <td>${escapar(m.productoNombre)}</td>
            <td><span class="badge ${clase}">${m.tipo}</span></td>
            <td class="num">${signo}${m.cantidad}</td>
            <td class="num">${m.stockResultante}</td>
            <td>${escapar(m.motivo || "—")}</td>`;
        tbody.appendChild(tr);
    }
}

function escapar(txt) {
    const d = document.createElement("div");
    d.textContent = txt;
    return d.innerHTML;
}

function actualizarDatalistCategorias() {
    const cats = [...new Set(productosCache.map((p) => p.categoria).filter(Boolean))].sort();
    $("#categorias").innerHTML = cats.map((c) => `<option value="${escapar(c)}">`).join("");
}

async function refrescarTodo() {
    await Promise.all([cargarResumen(), cargarProductos()]);
}

// ---------- pestañas ----------

document.querySelectorAll(".tab").forEach((btn) => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".tab").forEach((b) => b.classList.remove("is-active"));
        document.querySelectorAll(".tabpanel").forEach((p) => (p.hidden = true));
        btn.classList.add("is-active");
        const destino = btn.dataset.tab;
        $("#tab-" + destino).hidden = false;
        if (destino === "stockBajo") cargarStockBajo();
        if (destino === "movimientos") cargarMovimientos();
    });
});

// ---------- buscador ----------

let buscarTimer;
$("#buscador").addEventListener("input", () => {
    clearTimeout(buscarTimer);
    buscarTimer = setTimeout(cargarProductos, 250);
});

// ---------- modal producto ----------

const dlgProducto = $("#dlgProducto");
const formProducto = $("#formProducto");
let editandoId = null;

$("#btnNuevo").addEventListener("click", () => abrirFormProducto());

function abrirFormProducto(producto = null) {
    editandoId = producto ? producto.id : null;
    $("#dlgProductoTitulo").textContent = producto ? "Editar producto" : "Nuevo producto";
    $("#errProducto").hidden = true;
    formProducto.reset();
    if (producto) {
        formProducto.nombre.value = producto.nombre;
        formProducto.categoria.value = producto.categoria || "";
        formProducto.precio.value = producto.precio;
        formProducto.stock.value = producto.stock;
        formProducto.stockMinimo.value = producto.stockMinimo;
    }
    dlgProducto.showModal();
}

formProducto.addEventListener("submit", async (e) => {
    // el botón "cancel" cierra el dialog sin llegar aquí (value=cancel)
    if (e.submitter && e.submitter.value === "cancel") return;
    e.preventDefault();

    const datos = {
        nombre: formProducto.nombre.value.trim(),
        categoria: formProducto.categoria.value.trim(),
        precio: parseFloat(formProducto.precio.value),
        stock: parseInt(formProducto.stock.value, 10),
        stockMinimo: parseInt(formProducto.stockMinimo.value, 10),
    };

    try {
        if (editandoId) {
            await api("/productos/" + editandoId, { method: "PUT", body: JSON.stringify(datos) });
            toast("Producto actualizado");
        } else {
            await api("/productos", { method: "POST", body: JSON.stringify(datos) });
            toast("Producto creado");
        }
        dlgProducto.close();
        await refrescarTodo();
    } catch (err) {
        const p = $("#errProducto");
        p.textContent = err.message;
        p.hidden = false;
    }
});

// ---------- modal movimiento ----------

const dlgMovimiento = $("#dlgMovimiento");
const formMovimiento = $("#formMovimiento");

function abrirFormMovimiento(producto) {
    formMovimiento.reset();
    $("#errMovimiento").hidden = true;
    formMovimiento.productoId.value = producto.id;
    $("#movProductoNombre").textContent = `${producto.nombre} — stock actual: ${producto.stock}`;
    dlgMovimiento.showModal();
}

formMovimiento.addEventListener("submit", async (e) => {
    if (e.submitter && e.submitter.value === "cancel") return;
    e.preventDefault();

    const datos = {
        productoId: parseInt(formMovimiento.productoId.value, 10),
        tipo: formMovimiento.tipo.value,
        cantidad: parseInt(formMovimiento.cantidad.value, 10),
        motivo: formMovimiento.motivo.value.trim(),
    };

    try {
        await api("/movimientos", { method: "POST", body: JSON.stringify(datos) });
        toast("Movimiento registrado");
        dlgMovimiento.close();
        await refrescarTodo();
        if (!$("#tab-stockBajo").hidden) cargarStockBajo();
        if (!$("#tab-movimientos").hidden) cargarMovimientos();
    } catch (err) {
        const p = $("#errMovimiento");
        p.textContent = err.message;
        p.hidden = false;
    }
});

// ---------- acciones de las tablas (delegación de eventos) ----------

document.addEventListener("click", async (e) => {
    const btn = e.target.closest("button");
    if (!btn) return;

    if (btn.dataset.editar) {
        const p = productosCache.find((x) => x.id === Number(btn.dataset.editar));
        if (p) abrirFormProducto(p);
    }

    if (btn.dataset.mov) {
        let p = productosCache.find((x) => x.id === Number(btn.dataset.mov));
        if (!p) p = await api("/productos/" + btn.dataset.mov);
        abrirFormMovimiento(p);
    }

    if (btn.dataset.borrar) {
        const p = productosCache.find((x) => x.id === Number(btn.dataset.borrar));
        if (p && confirm(`¿Borrar "${p.nombre}" y su historial de movimientos?`)) {
            try {
                await api("/productos/" + p.id, { method: "DELETE" });
                toast("Producto borrado");
                await refrescarTodo();
            } catch (err) {
                toast(err.message, true);
            }
        }
    }
});

// ---------- arranque ----------

refrescarTodo().catch((err) => toast("No se pudo cargar: " + err.message, true));
