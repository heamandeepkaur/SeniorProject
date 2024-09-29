
function addProduct() {
    const form = document.getElementById('inventoryForm');
    let formData = new FormData(form);
    console.log(formData)

    // Prevent form submission
    event.preventDefault();

    fetch('/product/addProduct', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
        },
        body: JSON.stringify(Object.fromEntries(formData))
    })
    .then(function (response) {
        if (response.ok) {
            window.location.href = '/products';
        } else {
            // Handle errors, you can display a message to the user
            console.error('Error:', response.statusText);
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
}