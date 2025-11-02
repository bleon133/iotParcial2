// === Control de tema dark/light con guardado en localStorage ===
document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.getElementById("theme-toggle");
    const html = document.documentElement;

    // Verificar tema almacenado
    const savedTheme = localStorage.getItem("theme");
    if (savedTheme) {
        html.setAttribute("data-theme", savedTheme);
        updateIcon(savedTheme);
    } else {
        html.setAttribute("data-theme", "light");
    }

    // Escuchar el cambio manual
    toggle?.addEventListener("click", () => {
        const current = html.getAttribute("data-theme");
        const next = current === "light" ? "dark" : "light";
        html.setAttribute("data-theme", next);
        localStorage.setItem("theme", next);
        updateIcon(next);
    });

    function updateIcon(theme) {
        const icon = toggle.querySelector("i");
        if (!icon) return;
        icon.className = theme === "dark" ? "bi bi-sun-fill" : "bi bi-moon-stars-fill";
        toggle.querySelector("span").textContent = theme === "dark" ? "Modo Claro" : "Modo Oscuro";
    }
});
