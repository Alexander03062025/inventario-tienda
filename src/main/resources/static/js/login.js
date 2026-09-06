"use strict";

/* Pantalla de login: pide el token a /api/auth/login y lo guarda. */

const form = document.querySelector("#formLogin");
const err = document.querySelector("#errLogin");
const btn = document.querySelector("#btnEntrar");

// Si ya hay sesión, no tiene sentido quedarse aquí.
if (localStorage.getItem("token")) {
    location.replace("index.html");
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    err.hidden = true;
    btn.disabled = true;
    btn.textContent = "Entrando…";

    try {
        const res = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username: form.username.value.trim(),
                password: form.password.value,
            }),
        });
        const cuerpo = await res.json().catch(() => ({}));
        if (!res.ok) {
            throw new Error(cuerpo.detail || "No se pudo iniciar sesión");
        }

        // cuerpo = { token, username, nombre, rol, expiraEnMs }
        localStorage.setItem("token", cuerpo.token);
        localStorage.setItem("usuario", JSON.stringify({
            username: cuerpo.username,
            nombre: cuerpo.nombre,
            rol: cuerpo.rol,
        }));
        location.replace("index.html");
    } catch (ex) {
        err.textContent = ex.message;
        err.hidden = false;
        btn.disabled = false;
        btn.textContent = "Entrar";
    }
});
