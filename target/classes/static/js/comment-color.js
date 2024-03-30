
var comments = document.querySelectorAll('.comment');

comments.forEach(function (comment) {
    //We take the id of each comment
    var commentId = comment.id.split('-')[1];
    var score = parseInt(comment.querySelector('.score-container h5').innerText);

    var bgColor = '';
    if (score > 8) bgColor = 'green';
    else if (score > 5 && score <=8) bgColor = 'yellow';
    else bgColor = 'red';

    comment.querySelector('.score-container').style.backgroundColor = bgColor
});