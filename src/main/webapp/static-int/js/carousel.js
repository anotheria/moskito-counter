$(document).ready(function() {

    $("#carousel").carouFredSel({
        scroll: {
            fx: "crossfade",
            duration: 1000,
            timeoutDuration: 7000
        },
        auto: {
            pauseOnHover: 'resume'
        },
        prev: {
            button: "#carouselPrev",
            key: "left"
        },
        next: {
            button: "#carouselNext",
            key	: "right"
        }
    });

    $(".features-list ul").carouFredSel({
        width   : "100%",
        items: 3,
        scroll	: {
            items: 2,
            duration: 1000,
            timeoutDuration: 7000
        },
        auto    : {
            pauseOnHover: 'resume'
        },
        swipe: {
            onTouch: true,
            onMouse: true
        },
        pagination  : ".features-pagination"
    });

    $(window).resize(function(){
        if ($(window).width() < 979) {
            $(".features-list ul").trigger("configuration", {
                responsive: false,
                items: 2
            });
        }
        else {
            $(".features-list ul").trigger("configuration", {
                responsive: false,
                items: 3
            });
        }
        if ($(window).width() < 768) {
            $(".features-list ul").trigger("configuration", {
                responsive: true,
                items: {
                   visible: 2,
                   height: "auto"
                }

            });
        }
    });

    if ($(window).width() < 979) {
        $(".features-list ul").trigger("configuration", {
            items: 2
        });
    }
    if ($(window).width() < 768) {
        $(".features-list ul").carouFredSel({
            responsive: true,
            width   : "100%",
            items: 2,
            scroll	: {
                items: 2,
                duration: 1000,
                timeoutDuration: 7000
            },
            auto    : {
                pauseOnHover: 'resume'
            },
            swipe: {
                onTouch: true,
                onMouse: true
            },
            pagination  : ".features-pagination"
        });
    }

});