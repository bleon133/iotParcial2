(function ($) {
    const storageSidebar = 'sb:collapsed';
    const storageTheme = 'theme';
    const $body = $('body');
    const $html = $('html');
    const mqMobile = window.matchMedia('(max-width: 991.98px)');

    function setDesktopAria(collapsed) {
        $('.sidebar-toggle').attr('aria-pressed', collapsed ? 'true' : 'false');
    }
    function setMobileAria(open) {
        $('.sidebar-fab, .sidebar-toggle').attr('aria-expanded', open ? 'true' : 'false');
    }

    function restoreDesktopState() {
        const saved = localStorage.getItem(storageSidebar);
        const collapsed = (saved === '1');
        if (!mqMobile.matches && collapsed) {
            $body.addClass('sidebar-collapsed');
        } else if (mqMobile.matches) {
            $body.removeClass('sidebar-collapsed');
        }
        setDesktopAria(collapsed);
    }

    // ===============================
    // THEME HANDLING
    // ===============================
    function updateThemeButton(theme) {
        const $btn = $('#themeToggle');
        if (!$btn.length) return;

        if (theme === 'dark') {
            $btn.html('<i class="bi bi-moon-stars-fill me-2"></i>Modo Oscuro');
            $btn.attr('aria-pressed', 'true');
        } else {
            $btn.html('<i class="bi bi-sun-fill me-2"></i>Modo Claro');
            $btn.attr('aria-pressed', 'false');
        }
    }

    function applyTheme(theme) {
        $html.attr('data-theme', theme);
        updateThemeButton(theme);
    }

    function restoreTheme() {
        const saved = localStorage.getItem(storageTheme);
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        const theme = saved || (prefersDark ? 'dark' : 'light');
        applyTheme(theme);
    }

    // ===============================
    // SIDEBAR TOGGLE
    // ===============================
    window.toggleSidebar = function () {
        if (mqMobile.matches) {
            const isOpen = $body.toggleClass('sidebar-open').hasClass('sidebar-open');
            setMobileAria(isOpen);
        } else {
            const isCollapsed = $body.toggleClass('sidebar-collapsed').hasClass('sidebar-collapsed');
            setDesktopAria(isCollapsed);
            try { localStorage.setItem(storageSidebar, isCollapsed ? '1' : '0'); } catch (e) { }
        }
    };

    window.closeSidebar = function () {
        $body.removeClass('sidebar-open');
        setMobileAria(false);
    };

    // ===============================
    // EVENTOS
    // ===============================
    $(document).on('click', '#themeToggle', function () {
        const current = $html.attr('data-theme') || 'light';
        const next = current === 'light' ? 'dark' : 'light';
        applyTheme(next);
        try { localStorage.setItem(storageTheme, next); } catch (e) { }
    });

    $(document).on('keydown', function (e) {
        if (e.key === 'Escape') window.closeSidebar();
    });

    mqMobile.addEventListener('change', function (e) {
        $body.removeClass('sidebar-open');
        setMobileAria(false);
        if (e.matches) {
            $body.removeClass('sidebar-collapsed');
            setDesktopAria(false);
        } else {
            restoreDesktopState();
        }
    });

    // ===============================
    // INIT
    // ===============================
    $(function () {
        restoreDesktopState();
        restoreTheme();
    });
})(jQuery);
