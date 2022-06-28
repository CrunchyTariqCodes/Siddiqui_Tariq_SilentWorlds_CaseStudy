//Loading Icon
function showLoadingIcon() {
    $(".loading-icon").removeAttr("style").addClass("show");
}
function hideLoadingIcon() {
    $(".loading-icon").css("display", "none");
}