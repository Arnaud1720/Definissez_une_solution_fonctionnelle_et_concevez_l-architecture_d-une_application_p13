package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.services.InvoiceService;
import com.lowagie.text.*;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    // Couleurs de l'entreprise
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Bleu
    private static final Color GRAY_COLOR = new Color(107, 114, 128);
    private static final Color LIGHT_GRAY = new Color(243, 244, 246);

    @Override
    public byte[] generateInvoicePdf(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            long priceHT,
            long tvaAmount,
            long priceTTC
    ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
            Font grayFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_COLOR);
            Font bigBoldFont = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);

            // === EN-TÊTE ===
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 1});

            // Logo et nom entreprise
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            Paragraph company = new Paragraph();
            company.add(new Chunk("🚗 ", new Font(Font.HELVETICA, 20)));
            company.add(new Chunk("Your Car Your Way", titleFont));
            company.add(Chunk.NEWLINE);
            company.add(new Chunk("Location de véhicules en Europe", grayFont));
            logoCell.addElement(company);
            headerTable.addCell(logoCell);

            // Infos facture
            PdfPCell invoiceInfoCell = new PdfPCell();
            invoiceInfoCell.setBorder(Rectangle.NO_BORDER);
            invoiceInfoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph invoiceInfo = new Paragraph();
            invoiceInfo.setAlignment(Element.ALIGN_RIGHT);
            invoiceInfo.add(new Chunk("FACTURE", new Font(Font.HELVETICA, 18, Font.BOLD, PRIMARY_COLOR)));
            invoiceInfo.add(Chunk.NEWLINE);
            invoiceInfo.add(new Chunk("N° " + generateInvoiceNumber(rental.getId()), boldFont));
            invoiceInfo.add(Chunk.NEWLINE);
            invoiceInfo.add(new Chunk("Date : " + LocalDateTime.now().format(DATE_ONLY_FORMATTER), normalFont));
            invoiceInfoCell.addElement(invoiceInfo);
            headerTable.addCell(invoiceInfoCell);

            document.add(headerTable);
            document.add(new Paragraph(" ")); // Espace

            // === LIGNE SÉPARATRICE ===
            PdfPTable separator = new PdfPTable(1);
            separator.setWidthPercentage(100);
            PdfPCell sepCell = new PdfPCell();
            sepCell.setBackgroundColor(PRIMARY_COLOR);
            sepCell.setFixedHeight(3);
            sepCell.setBorder(Rectangle.NO_BORDER);
            separator.addCell(sepCell);
            document.add(separator);
            document.add(new Paragraph(" "));

            // === INFORMATIONS CLIENT ===
            PdfPTable clientTable = new PdfPTable(2);
            clientTable.setWidthPercentage(100);
            clientTable.setWidths(new float[]{1, 1});

            // Émetteur
            PdfPCell emitterCell = new PdfPCell();
            emitterCell.setBorder(Rectangle.NO_BORDER);
            emitterCell.setBackgroundColor(LIGHT_GRAY);
            emitterCell.setPadding(10);
            Paragraph emitter = new Paragraph();
            emitter.add(new Chunk("ÉMETTEUR\n", boldFont));
            emitter.add(new Chunk("Your Car Your Way SARL\n", normalFont));
            emitter.add(new Chunk("123 Avenue de la Location\n", normalFont));
            emitter.add(new Chunk("75001 Paris, France\n", normalFont));
            emitter.add(new Chunk("SIRET : 123 456 789 00012\n", grayFont));
            emitter.add(new Chunk("TVA : FR12345678901", grayFont));
            emitterCell.addElement(emitter);
            clientTable.addCell(emitterCell);

            // Client
            PdfPCell clientCell = new PdfPCell();
            clientCell.setBorder(Rectangle.NO_BORDER);
            clientCell.setBackgroundColor(LIGHT_GRAY);
            clientCell.setPadding(10);
            Paragraph client = new Paragraph();
            client.add(new Chunk("CLIENT\n", boldFont));
            client.add(new Chunk(user.getFirstName() + " " + user.getLastName() + "\n", normalFont));
            client.add(new Chunk(user.getEmail() + "\n", normalFont));
            clientCell.addElement(client);
            clientTable.addCell(clientCell);

            document.add(clientTable);
            document.add(new Paragraph(" "));

            // === DÉTAILS DE LA RÉSERVATION ===
            Paragraph reservationTitle = new Paragraph("DÉTAILS DE LA RÉSERVATION", bigBoldFont);
            document.add(reservationTitle);
            document.add(new Paragraph(" "));

            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{1, 2});

            addDetailRow(detailsTable, "N° Réservation", "#" + rental.getId(), boldFont, normalFont);
            addDetailRow(detailsTable, "Catégorie véhicule", getCategoryName(rental.getCatCar()), boldFont, normalFont);
            addDetailRow(detailsTable, "Agence de départ", departureAgency.getName() + "\n" + departureAgency.getCity() + ", " + departureAgency.getCountry(), boldFont, normalFont);
            addDetailRow(detailsTable, "Date de départ", rental.getStartDate().format(DATE_FORMATTER), boldFont, normalFont);
            addDetailRow(detailsTable, "Agence de retour", returnAgency.getName() + "\n" + returnAgency.getCity() + ", " + returnAgency.getCountry(), boldFont, normalFont);
            addDetailRow(detailsTable, "Date de retour", rental.getEndDate().format(DATE_FORMATTER), boldFont, normalFont);

            document.add(detailsTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // === TABLEAU DES PRIX ===
            Paragraph priceTitle = new Paragraph("DÉTAIL DES MONTANTS", bigBoldFont);
            document.add(priceTitle);
            document.add(new Paragraph(" "));

            PdfPTable priceTable = new PdfPTable(4);
            priceTable.setWidthPercentage(100);
            priceTable.setWidths(new float[]{3, 1, 1, 1});

            // En-tête du tableau
            addPriceHeaderCell(priceTable, "Description", headerFont);
            addPriceHeaderCell(priceTable, "Qté", headerFont);
            addPriceHeaderCell(priceTable, "Prix unitaire", headerFont);
            addPriceHeaderCell(priceTable, "Total", headerFont);

            // Ligne : Location
            addPriceDataCell(priceTable, "Location véhicule catégorie " + getCategoryName(rental.getCatCar()), normalFont, Element.ALIGN_LEFT);
            addPriceDataCell(priceTable, "1", normalFont, Element.ALIGN_CENTER);
            addPriceDataCell(priceTable, formatPrice(priceHT), normalFont, Element.ALIGN_RIGHT);
            addPriceDataCell(priceTable, formatPrice(priceHT), normalFont, Element.ALIGN_RIGHT);

            document.add(priceTable);

            // === TOTAUX ===
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(50);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addTotalRow(totalsTable, "Sous-total HT", formatPrice(priceHT), normalFont);
            addTotalRow(totalsTable, "TVA (20%)", formatPrice(tvaAmount), normalFont);

            // Total TTC en gras et plus grand
            PdfPCell ttcLabelCell = new PdfPCell(new Phrase("TOTAL TTC", new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));
            ttcLabelCell.setBackgroundColor(PRIMARY_COLOR);
            ttcLabelCell.setPadding(8);
            ttcLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalsTable.addCell(ttcLabelCell);

            PdfPCell ttcValueCell = new PdfPCell(new Phrase(formatPrice(priceTTC), new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));
            ttcValueCell.setBackgroundColor(PRIMARY_COLOR);
            ttcValueCell.setPadding(8);
            ttcValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.addCell(ttcValueCell);

            document.add(totalsTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // === PIED DE PAGE ===
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Chunk("Merci pour votre confiance !\n", boldFont));
            footer.add(new Chunk("Cette facture a été générée automatiquement et fait foi.\n", grayFont));
            footer.add(new Chunk("Pour toute question : support@yourcaryourway.com | +33 1 23 45 67 **", grayFont));
            document.add(footer);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de la facture PDF", e);
        }

        return baos.toByteArray();
    }

    private void addDetailRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(LIGHT_GRAY);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(LIGHT_GRAY);
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }

    private void addPriceHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addPriceDataCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(5);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(5);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String generateInvoiceNumber(Integer rentalId) {
        return "YCYW-" + LocalDateTime.now().getYear() + "-" + String.format("%06d", rentalId);
    }

    private String formatPrice(long priceInCents) {
        return CURRENCY_FORMATTER.format(priceInCents / 100.0);
    }

    private String getCategoryName(String code) {
        return switch (code) {
            case "A" -> "Économique";
            case "B" -> "Compacte";
            case "C" -> "Berline";
            case "D" -> "SUV";
            case "E" -> "Premium";
            default -> code;
        };
    }
}
