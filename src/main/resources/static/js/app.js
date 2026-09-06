"use strict";

/*
 * Frontend en JavaScript puro. Habla con la API REST de Spring Boot
 * usando fetch(). Envía el token JWT en cada petición.
 */

const API = "/api";

// ---------- sesión ----------

const token = localStorage.getItem("token");
const usuario = JSON.parse(localStorage.getItem("usuario") || "null");

// Sin token no se puede estar aquí: a la pantalla de login.
if (!token || !usuario) {
    location.replace("login.html");
}

const esAdmin = usuario && usuario.rol === "ADMIN";

function cerrarSesion() {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    location.replace("login.html");
}

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

/** Envuelve fetch: añade el token, y traduce errores del backend a Error. */
async function api(ruta, opciones = {}) {
    const res = await fetch(API + ruta, {
        headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token,
        },
        ...opciones,
    });

    if (res.status === 401) {
        cerrarSesion();
        throw new Error("Sesión expirada");
    }
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
        const accionesAdmin = esAdmin
            ? `<button class="btn btn--icon" data-editar="${p.id}">Editar</button>
               <button class="btn btn--icon btn--danger" data-borrar="${p.id}">Borrar</button>`
            : "";
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
                    ${accionesAdmin}
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

async function cargarUsuarios() {
    const lista = await api("/usuarios");
    const tbody = $("#tbodyUsuarios");
    tbody.innerHTML = "";
    for (const u of lista) {
        const tr = document.createElement("tr");
        const accion = u.username === usuario.username
            ? '<span style="color:#6b7683">tú</span>'
            : `<button class="btn btn--icon" data-toggle-usuario="${u.id}" data-activo="${u.activo}">
                 ${u.activo ? "Desactivar" : "Activar"}
               </button>`;
        tr.innerHTML = `
            <td>${escapar(u.username)}</td>
            <td>${escapar(u.nombre)}</td>
            <td><span class="badge">${u.rol}</span></td>
            <td>${u.activo ? "Activo" : '<span style="color:#dc2626">Inactivo</span>'}</td>
            <td>${accion}</td>`;
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

// ---------- UI según sesión / rol ----------

function aplicarSesionEnUI() {
    $("#usuarioInfo").textContent = `${usuario.nombre} · ${usuario.rol}`;
    $("#btnSalir").addEventListener("click", cerrarSesion);

    if (esAdmin) {
        document.querySelectorAll(".solo-admin").forEach((el) => (el.hidden = false));
    } else {
        // el vendedor no crea productos
        $("#btnNuevo").hidden = true;
    }
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
        if (destino === "usuarios") cargarUsuarios();
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

// ---------- modal usuario (solo admin) ----------

const dlgUsuario = $("#dlgUsuario");
const formUsuario = $("#formUsuario");

const btnNuevoUsuario = $("#btnNuevoUsuario");
if (btnNuevoUsuario) {
    btnNuevoUsuario.addEventListener("click", () => {
        formUsuario.reset();
        $("#errUsuario").hidden = true;
        dlgUsuario.showModal();
    });
}

formUsuario.addEventListener("submit", async (e) => {
    if (e.submitter && e.submitter.value === "cancel") return;
    e.preventDefault();

    const datos = {
        username: formUsuario.username.value.trim(),
        nombre: formUsuario.nombre.value.trim(),
        password: formUsuario.password.value,
        rol: formUsuario.rol.value,
    };

    try {
        await api("/usuarios", { method: "POST", body: JSON.stringify(datos) });
        toast("Usuario creado");
        dlgUsuario.close();
        cargarUsuarios();
    } catch (err) {
        const p = $("#errUsuario");
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

    if (btn.dataset.toggleUsuario) {
        const activar = btn.dataset.activo !== "true";
        try {
            await api(`/usuarios/${btn.dataset.toggleUsuario}/estado?activo=${activar}`, { method: "PATCH" });
            toast(activar ? "Usuario activado" : "Usuario desactivado");
            cargarUsuarios();
        } catch (err) {
            toast(err.message, true);
        }
    }
});

// ---------- arranque ----------

aplicarSesionEnUI();
refrescarTodo().catch((err) => toast("No se pudo cargar: " + err.message, true));
