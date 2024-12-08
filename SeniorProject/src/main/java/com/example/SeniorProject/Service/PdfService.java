package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.Customer;
import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderProduct;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfService
{
    @Autowired
    private S3Service s3Service;
    public File generateInvoicePDF(Map<String, Object> model) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set font and leading
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.setLeading(18f);

            // Company Name and Invoice Title
            contentStream.beginText();
            contentStream.newLineAtOffset(220, 750); // Centered
            contentStream.showText("Lux Party Rents"); // Company Name
            contentStream.endText();

            // Draw a line below the company name
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(50, 740);
            contentStream.lineTo(550, 740);
            contentStream.stroke();

            // Invoice Information (top right)
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(400, 720);
            contentStream.showText("INVOICE");
            contentStream.newLine();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Invoice #: " + ((Order) model.get("order")).getId());
            contentStream.newLine();
            contentStream.showText("Date: " + ((Order) model.get("order")).getCreationDate().toString());
            contentStream.endText();

            // Bill To Section
            Customer customer = (Customer) model.get("customer");
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, 670);
            contentStream.showText("BILL TO");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 650);
            contentStream.showText(customer.getFirstName() + " " + customer.getLastName());
            contentStream.newLine();
            contentStream.showText("Phone: " + customer.getPhone());
            contentStream.newLine();
            contentStream.showText("Email: " + customer.getEmail());
            contentStream.endText();

            // Adding more space after email (by adjusting yPosition)
            float yPosition = 600; // Adjusted to give more space

            // Add more spacing before the table header to avoid overlap
            contentStream.setLineWidth(1f);
            contentStream.moveTo(50, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();

            // Table Header - Description, Quantity, Price
            yPosition -= 10; // Give space above the table
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("NAME");
            contentStream.newLineAtOffset(350, 0);
            contentStream.showText("QUANTITY");
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText("PRICE");
            contentStream.endText();

            // Draw a line below the header
            yPosition -= 20;
            contentStream.moveTo(50, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();

            yPosition -= 20; // Start below the header

            // Table Rows - Product List
            for (OrderProduct orderProduct : ((Order) model.get("order")).getOrderProducts()) {
                String productName = orderProduct.getProduct().getName();
                int quantity = orderProduct.getQuantity();
                double productPrice = orderProduct.getProduct().getPrice() * orderProduct.getQuantity();

                // Description (left column)
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(productName);
                contentStream.endText();

                // Quantity (middle column)
                contentStream.beginText();
                contentStream.newLineAtOffset(400, yPosition);
                contentStream.showText(String.valueOf(quantity));
                contentStream.endText();

                // Price (right column)
                contentStream.beginText();
                contentStream.newLineAtOffset(500, yPosition);
                contentStream.showText(String.format("$%.2f", productPrice));
                contentStream.endText();

                yPosition -= 20; // Move to the next row

                // Draw a line after each product row
                contentStream.moveTo(50, yPosition + 10);
                contentStream.lineTo(550, yPosition + 10);
                contentStream.stroke();
            }

            // Subtotal, Tax, and Total - at the bottom
            double price = ((Order) model.get("order")).getPrice();
            double tax = ((Order) model.get("order")).getTax(); // Assuming 7% tax
            double securityDeposit = ((Order) model.get("order")).getDeposit();
            double deliveryFee = ((Order) model.get("order")).getDeliveryFee();
            double subtotal = ((Order) model.get("order")).getSubtotal();


            // Moving yPosition down for each new line to prevent overlapping
            yPosition -= 20; // Initial offset for price section

// Price
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Price:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(500, yPosition);
            contentStream.showText(String.format("$%.2f", price));
            contentStream.endText();

// Tax
            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Tax (7.25%):");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(500, yPosition);
            contentStream.showText(String.format("$%.2f", tax));
            contentStream.endText();

// Security Deposit
            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Security Deposit:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(500, yPosition);
            contentStream.showText(String.format("$%.2f", securityDeposit));
            contentStream.endText();

// Delivery Fee
            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Delivery Fee:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(500, yPosition);
            contentStream.showText(String.format("$%.2f", deliveryFee));
            contentStream.endText();
// Total
            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("SUBTOTAL:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(500, yPosition);
            contentStream.showText(String.format("$%.2f", subtotal));
            contentStream.endText();

            // Thank You Note
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, yPosition - 100);
            contentStream.showText("Thank you for your business with us!");
            contentStream.endText();

            // Close the content stream
            contentStream.close();

            // Write the PDF content to a byte array
            File outputFile = File.createTempFile("Invoice_" + ((Order) model.get("order")).getId(), ".pdf");
            document.save(outputFile);
            s3Service.uploadOrderInvoice(outputFile, ((Order) model.get("order")).getId());
            return outputFile;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    public File downloadPDFFromCLoudFront(String fileName)
    {
        String url = "https://d3snlw7xiuobl9.cloudfront.net/" + fileName;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        if(response.getStatusCode().is2xxSuccessful())
        {
            byte[] pdfByte = response.getBody();
            File tempFile = null;
            try
            {
                tempFile = File.createTempFile("downloaded-", ".pdf");
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            try (FileOutputStream fos = new FileOutputStream(tempFile))
            {
                fos.write(pdfByte);
                return tempFile;
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            throw new RuntimeException("Error downloading PDF");
        }
    }
}
