window.onload = function() {
    fetch('/product/getAll', {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
            "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
        }
    })
    .then(response => response.json())
    .then(res => {
        const data = res;
        let rows = '';
        data.forEach(product => {
            rows += `<tr data-id="${product.id}">
                        <td>${product.id}</td>
                        <td>${product.name}</td>
                        <td>${product.price}</td>
                        <td>${product.quantity}</td>
                        <td>${product.type}</td>
                     </tr>`;
        });
        console.log(rows);
        document.getElementById('tableRows').innerHTML = rows;

        // Add click event listeners to each row
        const tableRows = document.querySelectorAll('#tableRows tr');
        tableRows.forEach(row => {
            row.addEventListener('click', function() {
                const productId = this.dataset.id; // Get the product ID
                window.location.href = `/theProduct?id=${productId}`; // Redirect to the product page
            });
        });
    })
    .catch(error => console.log('Error fetching data:', error));
};


function updateProduct() {
  const form = document.getElementById('theProduct');
  let formData = new FormData(form);
  fetch('/product/update',
    {
      method: 'POST',
      headers:
      {
        'Content-Type': 'application/json',
        "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
      },
      body: JSON.stringify(Object.fromEntries(formData))
    })
    .then(function (response) {
      if (response.status === 200) {
        window.location.href = '/products';
      } else {
        //error handling
      }
    })
    .catch(error => {
      console.error('Error:', error);
    });

}

