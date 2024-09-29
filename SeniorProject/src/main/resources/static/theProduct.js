window.onload = function() {
    const params = new URLSearchParams(window.location.search);
    const productId = params.get('id'); // Get the product ID from the URL

    if (productId) {
        fetch(`/product/getById?id=${productId}`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
            }
        })
        .then(response => response.json())
        .then(product => {
            // Populate the form fields with the product details
            document.querySelector('.id').value = product.id;
            document.querySelector('.name').value = product.name;
            document.querySelector('.quantity').value = product.quantity;
            document.querySelector('.price').value = product.price;
            document.querySelector('.type').value = product.type;
            document.querySelector('.description').value = product.description;
        })
        .catch(error => console.log('Error fetching product details:', error));
    }
};

function updateProduct() {
    const form = document.getElementById('updateForm');
    let formData = new FormData(form);
    console.log(formData)
    // Prevent form submission
    event.preventDefault();

    fetch('/product/update', {
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
};

function deleteProduct() {
    const id = document.querySelector('.id').value; // Get the product ID from the hidden field

    fetch(`/product/delete?id=${id}`, {
        method: 'DELETE',
        headers: {
            "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
        }
    })
    .then(response => {
        if (response.ok) {
            window.location.href = '/products'; // Redirect on success
        } else {
            console.error('Error:', response.statusText);
            alert('Failed to delete product. Please try again.');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred. Please try again later.');
    });
}