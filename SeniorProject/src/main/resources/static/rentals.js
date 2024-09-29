async function loadProductNames(page = currentPage, size = pageSize) {
    try {
        const response = await fetch(`/product/getAll?page=${page}&size=${size}`);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const data = await response.json();
        const products = data.content; // Page content is in the 'content' field

        const container = document.getElementById('product-container');
        container.innerHTML = '';

        products.forEach(product => {
            const nameDiv = document.createElement('div');
            nameDiv.className = 'col item-name';
            nameDiv.textContent = product.name; // Display product name
            container.appendChild(nameDiv);
        });

        // Update pagination controls if necessary
        // For simplicity, add a "Next" button
        const nextButton = document.createElement('button');
        nextButton.textContent = 'Next';
        nextButton.onclick = () => {
            currentPage++;
            loadProductNames();
        };
        container.appendChild(nextButton);

    } catch (error) {
        console.error('Error fetching products:', error);
    }
}

window.onload = loadProductNames;