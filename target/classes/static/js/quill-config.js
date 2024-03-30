var quill = new Quill('#editor', {
    theme: 'snow'
});

// Escuchar los cambios en Quill y actualizar el textarea oculto
quill.on('text-change', function() {
    var html = document.querySelector('.ql-editor').innerHTML;
    document.querySelector('textarea[name="opinion"]').value = html;
});


/*let toolbaroptions = [
    [""]
]
let quill = new Quill("#editor", {
    modules: {
        toolbar: true,
    },
    theme:"snow"
})*/