// /js/nav/nav.js
(function ($) {
    const storageKey = 'sb:collapsed';
    const $body = $('body');
    const mqMobile = window.matchMedia('(max-width: 991.98px)');

    function setDesktopAria(collapsed){
        $('.sidebar-toggle').attr('aria-pressed', collapsed ? 'true' : 'false');
    }
    function setMobileAria(open){
        $('.sidebar-fab, .sidebar-toggle').attr('aria-expanded', open ? 'true' : 'false');
    }

    function restoreDesktopState() {
        const saved = localStorage.getItem(storageKey);
        const collapsed = (saved === '1');
        if (!mqMobile.matches && collapsed) {
            $body.addClass('sidebar-collapsed');
        } else if (mqMobile.matches) {
            $body.removeClass('sidebar-collapsed');
        }
        setDesktopAria(collapsed);
    }

    window.toggleSidebar = function () {
        if (mqMobile.matches) {
            const isOpen = $body.toggleClass('sidebar-open').hasClass('sidebar-open');
            setMobileAria(isOpen);
        } else {
            const isCollapsed = $body.toggleClass('sidebar-collapsed').hasClass('sidebar-collapsed');
            setDesktopAria(isCollapsed);
            try { localStorage.setItem(storageKey, isCollapsed ? '1' : '0'); } catch(e){}
        }
    };

    window.closeSidebar = function () {
        $body.removeClass('sidebar-open');
        setMobileAria(false);
    };

    // ESC cierra en móvil
    $(document).on('keydown', function (e) {
        if (e.key === 'Escape') window.closeSidebar();
    });

    // Cambios de viewport
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

    // Init
    $(restoreDesktopState);
})(jQuery);
