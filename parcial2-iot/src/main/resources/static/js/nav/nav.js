(function ($) {
    'use strict';

    var $body = $('body');
    var mqMobile = window.matchMedia('(max-width: 991.98px)');
    var storageKey = 'sb:collapsed';

    function setDesktopAria(collapsed) {
        $('.sidebar-toggle').attr('aria-pressed', collapsed ? 'true' : 'false');
    }

    function setMobileAria(open) {
        $('.sidebar-fab').attr('aria-expanded', open ? 'true' : 'false');
        $('.sidebar-toggle').attr('aria-expanded', open ? 'true' : 'false');
    }

    function restoreDesktopState() {
        var saved = null;
        try { saved = localStorage.getItem(storageKey); } catch (e) {}
        var collapsed = (saved === '1');

        if (!mqMobile.matches && collapsed) {
            $body.addClass('sidebar-collapsed');
        }
        setDesktopAria(collapsed);
    }

    // Exponer para los onclick del HTML
    window.toggleSidebar = function () {
        if (mqMobile.matches) {
            // Móvil: abrir/cerrar off-canvas
            $body.toggleClass('sidebar-open');
            var isOpen = $body.hasClass('sidebar-open');
            setMobileAria(isOpen);
        } else {
            // Desktop: colapsar/expandir ancho
            $body.toggleClass('sidebar-collapsed');
            var isCollapsed = $body.hasClass('sidebar-collapsed');
            setDesktopAria(isCollapsed);
            try { localStorage.setItem(storageKey, isCollapsed ? '1' : '0'); } catch (e) {}
        }
    };

    window.closeSidebar = function () {
        $body.removeClass('sidebar-open');
        setMobileAria(false);
    };

    // Cerrar con ESC en móvil
    $(document).on('keydown', function (e) {
        if (e.key === 'Escape') {
            window.closeSidebar();
        }
    });

    // Responder a cambios del media query (compatibilidad addEventListener / addListener)
    function onMqChange(e) {
        $body.removeClass('sidebar-open');
        setMobileAria(false);

        if (e.matches) {
            // Entró a móvil: forzamos expandido (sin colapsado visual)
            $body.removeClass('sidebar-collapsed');
            setDesktopAria(false);
        } else {
            // Volvió a desktop: restaurar preferencia
            restoreDesktopState();
        }
    }

    if (typeof mqMobile.addEventListener === 'function') {
        mqMobile.addEventListener('change', onMqChange);
    } else if (typeof mqMobile.addListener === 'function') {
        // Safari/antiguos
        mqMobile.addListener(onMqChange);
    }

    // Inicializar al cargar DOM
    $(function () {
        restoreDesktopState();
    });

})(jQuery);
