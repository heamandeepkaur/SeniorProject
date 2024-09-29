document.addEventListener('DOMContentLoaded', function() {

    // Simulate fetching gallery images
    const gallerySection = document.querySelector('.gallery');
    const images = [
        'https://d3snlw7xiuobl9.cloudfront.net/Image1.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Image2.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Image3.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Image4.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Image5.jpg',
    
        // Add as many paths as needed
    ];

    const displayImages = (imageArray) => {
        gallerySection.innerHTML = ''; // Clear current images
        imageArray.forEach(image => {
            const imgElement = document.createElement('img');
            imgElement.src = image;
            imgElement.alt = "Gallery image";
            gallerySection.appendChild(imgElement);
        });
    };

    // Initially display all images
    displayImages(images);

    // Search functionality
    const searchInput = document.querySelector('.nav-bar input[type="text"]');
    searchInput.addEventListener('keyup', (e) => {
        const filteredImages = images.filter(image => image.includes(e.target.value));
        displayImages(filteredImages);
    });
});
