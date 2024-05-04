document.querySelectorAll('.field .input').forEach(input => {
    input.addEventListener('input', function() {
        const label = this.parentElement.querySelector('.label');
        if (this.value.length > 0) {
            label.style.opacity = '0';
        } else {
            label.style.opacity = '1';
        }
    });
});