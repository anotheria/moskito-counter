$(document).ready(function () {
    $(window).scroll(function () {
        if ($(this).scrollTop() > 40) {
            $('.wrapper').addClass("scroll");
        } else if ($(this).scrollTop() <= 30 && $('.wrapper').hasClass("scroll")) {
            $('.wrapper').removeClass("scroll");
        }
    });//scroll

    $('.inactive span').tooltip({
        placement: 'bottom'
    });
});