/* This is the js file for the User Details Page.
 * The users page currently only contains temporary data
 * until it is hooked up with the back end when it is ready.
 * This data should be replaced when back end is ready.
 *
 * - Andreas Zignago (Last edit: 9/29/24)
 */

document.addEventListener("DOMContentLoaded", function(){

    const form = document.getElementById("user_details");
    const button1 = document.getElementById("button1");
    const fname = document.getElementById("fname");
    const lname = document.getElementById("lname");
    const phone = document.getElementById("phone");
    const email = document.getElementById("email");
    const address = document.getElementById("address");
    const location = document.getElementById("location");

    if(form){
        form.addEventListener("submit", function(event) {
        event.preventDefault(); // Prevent default form submission
        fname.disabled = !fname.disabled;
        lname.disabled = !lname.disabled;
        phone.disabled = !phone.disabled;
        email.disabled = !email.disabled;
        address.disabled = !address.disabled;
        location.disabled =  !location.disabled;
        if(!fname.disabled){
            button1.textContent = "Save";
        }else{
        button1.textContent = "Edit"
        }

        });
    }
});

/*
document.addEventListener("DOMContentLoaded", function() {
    var frontPageForm = document.getElementById("other-page-form");
    if (frontPageForm) {
        frontPageForm.addEventListener("submit", function(event) {
            event.preventDefault(); // Prevent default form submission
        });
    }
});
*/