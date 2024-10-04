package com.guman.bbc_backend.service;

import com.guman.bbc_backend.entity.ConsumerID;
import com.guman.bbc_backend.entity.Customer;
import com.guman.bbc_backend.entity.Invoice;
import com.guman.bbc_backend.repository.ConsumerIDRepository;
import com.guman.bbc_backend.repository.CustomerRepository;
import com.guman.bbc_backend.repository.InvoiceRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ConsumerIDRepository consumerIDRepository;

    // ... (other methods remain the same)

    private byte[] generatePdfContent(Customer customer, Invoice invoice) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Company details
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("BBC Company");
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Address: Finzly India");
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Phone: 1234567890");
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Email: bbc@bbc.com");
                contentStream.endText();

                // Invoice title and status
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 650);
                contentStream.showText("INVOICE");
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(375, 0);
                contentStream.showText("(" + invoice.getStatus() + ")");
                contentStream.endText();

                // Invoice details
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 600);
                contentStream.showText("Invoice Date: " + formatDate(invoice.getPaidDate()));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Due Date: " + formatDate(invoice.getBillDueDate()));
                contentStream.endText();

                // Customer details
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(300, 600);
                contentStream.showText("Bill To:");
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText(customer.getName());
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Phone: " + customer.getPhoneNumber());
                contentStream.endText();

                // Invoice items table
                float yStart = 500;
                float tableWidth = 500;
                float yPosition = yStart;
                float margin = 50;
                String[][] content = {
                    {"Description", "Period", "Consumption", "Amount"},
                    {"Electricity Charges", 
                     formatDate(invoice.getBillingStartDate()) + " - " + formatDate(invoice.getBillingEndDate()), 
                     invoice.getUnitConsumed() + " units", 
                     "Rs. " + invoice.getFinalAmount()}
                };


                drawTable(contentStream, yPosition, margin, content, tableWidth);

                // Totals
                yPosition = 400;
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(350, yPosition);
                contentStream.showText("Subtotal: Rs. " + invoice.getFinalAmount());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Early Payment Discount: Rs. " + invoice.getEarlyPaymentDiscount());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Online Payment Discount: Rs. " + invoice.getOnlinePaymentDiscount());
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Total Amount Due: Rs. " + invoice.getAmountDue());
                contentStream.endText();

                // Footer
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(margin, 30);
                contentStream.showText("Thank you for your business. For any queries, please contact our customer support.");
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private void drawTable(PDPageContentStream contentStream, float y, float margin, String[][] content, float tableWidth) throws IOException {
        final int rows = content.length;
        final int cols = content[0].length;
        final float rowHeight = 20f;
        final float tableHeight = rowHeight * rows;
        final float colWidth = tableWidth / (float) cols;

        // Draw table
        for (int i = 0; i <= rows; i++) {
            float yPosition = y - (i * rowHeight);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + tableWidth, yPosition);
            contentStream.stroke();
        }

        for (int i = 0; i <= cols; i++) {
            float xPosition = margin + (i * colWidth);
            contentStream.moveTo(xPosition, y);
            contentStream.lineTo(xPosition, y - tableHeight);
            contentStream.stroke();
        }

        // Add content
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

        float textx = margin + 2;
        float texty = y - 15;
        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[i].length; j++) {
                String text = content[i][j];
                contentStream.beginText();
                contentStream.newLineAtOffset(textx, texty);
                contentStream.showText(text);
                contentStream.endText();
                textx += colWidth;
            }
            texty -= rowHeight;
            textx = margin + 2;
        }
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    public Invoice processPayment(Integer invoiceId, boolean isOnlinePayment) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Apply discounts based on payment type
        BigDecimal finalAmount = invoice.getAmountDue();
        if (isOnlinePayment) {
            finalAmount = finalAmount.subtract(invoice.getOnlinePaymentDiscount());
        }
        // Always apply early payment discount as payment is made on due date
        finalAmount = finalAmount.subtract(invoice.getEarlyPaymentDiscount());

        invoice.setFinalAmount(finalAmount);
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaidDate(LocalDate.now());

        return invoiceRepository.save(invoice);
    }

    public byte[] generateInvoice(String phoneNumber, String connectionNumber, LocalDate startDate, LocalDate endDate) {
        Customer customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        ConsumerID consumerID = consumerIDRepository.findByConnectionId(connectionNumber)
                .orElseThrow(() -> new RuntimeException("Consumer ID not found"));

        Invoice invoice = invoiceRepository.findByConsumerIdAndBillingStartDateAndBillingEndDate(
                consumerID.getConsumerId(), startDate, endDate)
                .orElseThrow(() -> new RuntimeException("Invoice not found for the specified date range"));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            return generatePdfContent(customer, invoice);
        } else {
            throw new RuntimeException("Cannot generate invoice. The invoice for the specified date range is not paid.");
        }
    }

    public Invoice getInvoiceById(Integer invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    public List<Invoice> getInvoicesByPhoneNumber(String phoneNumber) {
        Customer customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        List<ConsumerID> consumerIDs = consumerIDRepository.findByUserId(customer.getUserId());
        
        return consumerIDs.stream()
                .flatMap(consumerID -> invoiceRepository.findByConsumerId(consumerID.getConsumerId()).stream())
                .collect(Collectors.toList());
    }

    public List<Invoice> getUnpaidInvoicesByConnectionId(String connectionId) {
        ConsumerID consumerID = consumerIDRepository.findByConnectionId(connectionId)
                .orElseThrow(() -> new RuntimeException("Consumer ID not found for connection ID: " + connectionId));
        return invoiceRepository.findByConsumerIdAndStatus(consumerID.getConsumerId(), Invoice.InvoiceStatus.PENDING);
    }

    // public List<Invoice> getAllInvoicesByConnectionId(String connectionId) {
    //     return invoiceRepository.findAllByConnectionId(connectionId);
    // }
}