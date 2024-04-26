window.onload = function() {
    // Obtener el contenedor del botón y del menú
    var identificationContainer = document.querySelector('.identification_logged');
    var menuContainer = document.getElementById('menuContainer');

    // Mostrar u ocultar el menú al hacer clic en el contenedor del botón
    identificationContainer.addEventListener('click', function () {
        if (menuContainer.style.display === 'none') {
            menuContainer.style.display = 'block';
        } else {
            menuContainer.style.display = 'none';
        }
    });

    // Ocultar el menú si se hace clic fuera de él
    document.addEventListener('click', function (event) {
        var target = event.target;
        if (!menuContainer.contains(target) && target !== identificationContainer) {
            menuContainer.style.display = 'none';
        }
    });
};