/* This is the js file for the shopping cart.
 * As of 9/14/24, the shopping cart will be hardcoded until
 * we completely implement the backend.
 * 
 * - Brandon Kue (Last edit: 9/14/24)
 */
// Divs needed 
let emptyCartDiv = document.getElementById("cartEmpty");
let emptyTotalDiv = document.getElementById("totalEmpty");
let itemCartDiv = document.getElementById("cartItemCard");
let itemTotalDiv = document.getElementById("totalItemCard");

// Will hardcode a nested array to act as a cart
let myCart = [
        {name: "Flowervase", price: 24.99, description: "A flower vase gotten from 1990 from Ronald Reagan.", amount: 1},
        {name: "Blackplate", price: 14.99, description: "This is a cool plate with a nice sleek design. Perfect for a wedding!", amount: 1}
];
let totalAmountOfItems = 0;
let totalCost = 0.00;
// If there is nothing in the array, we show the empty div
if (myCart.length == 0) {
    // show empty
    emptyCartDiv.classList.remove("d-none");
    emptyTotalDiv.classList.remove("d-none");
} else {
    // remove empty
    emptyCartDiv.classList.add("d-none");
    emptyTotalDiv.classList.add("d-none");
    // Get the total length of the array
    calculateTotalItems();
    console.log("Total items: " + totalAmountOfItems);
    // Calculate total
    calculateTotalCost();
    // Generate one item card per nested array and indicate the amount in each
    myCart.forEach(itemBucket => {
        duplicateCartItem(itemBucket);
        duplicateTotalItem(itemBucket);
    })
}

// Handle add button
document.getElementById("addButton").addEventListener("click", function() {
    let newItem = {
        name: "Goblet",
        price: 7.99,
        description: "This is an ancient goblet stolen from Voldemort.", // Optional: You can add description field to the form
        amount: 1
    };

    // Add the new item to the cart
    myCart.push(newItem);

    // Update the cart and total items list
    duplicateCartItem(newItem);
    duplicateTotalItem(newItem);
    calculateTotalItems();
    calculateTotalCost();
})

function duplicateCartItem(item) {
    // Get the div
    let originalCartItem = document.getElementById("cartItemCard")
    // Make a clone
    let clonedCartItem = originalCartItem.cloneNode(true);
    // set the image
    let clonedItemImage = clonedCartItem.querySelector("#itemImage");
    clonedItemImage.src = "/picture/items/" + item.name.toLowerCase() + ".jpg"; 
    // Change the name
    clonedCartItem.id = "cloned"+ item.name.replace(/\s+/g, '') +"CartItem"
    let clonedItemName = clonedCartItem.querySelector("#itemName");
    clonedItemName.innerHTML = item.name;

    let clonedItemDescription = clonedCartItem.querySelector("#itemDescription");
    clonedItemDescription.innerHTML = item.description;

    let clonedItemAmount = clonedCartItem.querySelector("#itemAmountInput");
    clonedItemAmount.id = "cloned" + item.name.replace(/\s+/g, '') + "CartAmount"
    clonedItemAmount.value = item.amount;

    let clonedItemCost = clonedCartItem.querySelector("#itemCost");
    clonedItemCost.id = "cloned" + item.name.replace(/\s+/g, '') + "CartCost"
    console.log(item.name + ": " + item.price);
    clonedItemCost.innerHTML = "$"+item.price;
    
    clonedItemAmount.addEventListener("input", function() {
        if (parseInt(this.value)) {
            // Reflect changes onto the otherside
            let cartItemAmountElement = document.querySelector("#cloned" + item.name.replace(/\s+/g, '') + "CartAmount");
            let totalItemAmountElement = document.querySelector("#cloned" + item.name.replace(/\s+/g, '') + "TotalAmount"); 
            cartItemAmountElement.value = this.value;
            totalItemAmountElement.innerHTML = "x"+this.value;
            // Recalculate Total
            let itemWanted = myCart.find(cartItem => cartItem.name === item.name);
            if (itemWanted) {
                itemWanted.amount = this.value;
                totalAmountOfItems = 0;
                totalCost = 0;
                calculateTotalItems();
                calculateTotalCost(); 
            }
        }
        
    })

    // Handle delete button
    let deleteButton = clonedCartItem.querySelector(".deleteButton");
    deleteButton.addEventListener("click", function() {
        console.log("Working");
        // Remove from array
        myCart = myCart.filter(cartItem => cartItem.name !== item.name);
        
        // Remove from DOM
        document.getElementById("shoppingCol").removeChild(clonedCartItem);

        // Remove from total items list
        let totalItemDiv = document.querySelector("#cloned" + item.name.replace(/\s+/g, '') + "TotalItem");
        if (totalItemDiv) {
            document.getElementById("itemCardContainer").removeChild(totalItemDiv);
        }
        
        // Recalculate totals
        totalAmountOfItems = 0;
        totalCost = 0;
        calculateTotalItems();
        calculateTotalCost();
        
        // Handle empty cart display
        if (myCart.length === 0) {
            emptyCartDiv.classList.remove("d-none");
            emptyTotalDiv.classList.remove("d-none");
        }
    });

    // Append
    clonedCartItem.classList.remove("d-none");
    document.getElementById("shoppingCol").appendChild(clonedCartItem);
}

function duplicateTotalItem(item) {
    // Get the div
    let originalTotalItem = document.getElementById("totalItemCard")
    // Make a clone
    let clonedTotalItem = originalTotalItem.cloneNode(true);
    // Change the name
    clonedTotalItem.id = "cloned" + item.name.replace(/\s+/g, '') + "TotalItem"
    let clonedItemName = clonedTotalItem.querySelector("#itemName");
    clonedItemName.innerHTML = item.name;

    let clonedItemAmount = clonedTotalItem.querySelector("#itemAmount");
    clonedItemAmount.id = "cloned" + item.name.replace(/\s+/g, '') + "TotalAmount"
    clonedItemAmount.innerHTML = "x"+item.amount;

    let clonedItemCost = clonedTotalItem.querySelector("#itemCost");
    clonedItemCost.id = "cloned" + item.name.replace(/\s+/g, '') + "TotalCost"
    clonedItemCost.innerHTML = "$"+item.price;

    // Append
    clonedTotalItem.classList.remove("d-none");
    document.getElementById("itemCardContainer").appendChild(clonedTotalItem);

}

function calculateTotalItems() {
    myCart.forEach(itemBucket => {
        totalAmountOfItems += parseInt(itemBucket.amount);
    });
    document.getElementById("totalItemCount").innerHTML = "Your Cart (" + totalAmountOfItems + ")";
}

function calculateTotalCost() {
    myCart.forEach(itemBucket => {
        let itemAmount = itemBucket.amount;
        console.log(itemBucket.name + ": " + itemAmount);
        let totalItemCost = itemAmount * itemBucket.price;
        totalCost += totalItemCost;
    }) 
    document.getElementById("completeTotal").innerHTML = "$"+totalCost.toFixed(2);
}

