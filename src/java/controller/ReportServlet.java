package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.util.logging.Logger;

public class ReportServlet extends HttpServlet {

    private Connection conn;
    private String secretKey;
    private String cipherAlgorithm;

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    private static final DateTimeFormatter TS_DISPLAY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

    private static final DateTimeFormatter TS_FILE =
            DateTimeFormatter.ofPattern("yyyyMMdd_hhmmss");

    private static final String ADMIN_TITLE = "USER ACCOUNTS";
    private static final String GUEST_TITLE = "ACCOUNT DETAILS";

    private static final Font FONT_TITLE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

    private static final Font FONT_TIMESTAMP =
            FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);

    private static final Font FONT_LABEL =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

    private static final Font FONT_VALUE =
            FontFactory.getFont(FontFactory.HELVETICA, 12);

    private static final Font FONT_TABLE =
            FontFactory.getFont(FontFactory.HELVETICA, 12);

    private static final Font FONT_TABLE_HEADER =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);

    @Override
    public void init() throws ServletException {
        try {
            secretKey = getServletContext().getInitParameter("secretKey");
            cipherAlgorithm = getServletContext().getInitParameter("cipherAlgorithm");

            Class.forName(getServletConfig().getInitParameter("jdbcClassName"));

            conn = DriverManager.getConnection(
                    getServletConfig().getInitParameter("jdbcDriverURL") + "://"
                    + getServletConfig().getInitParameter("dbHostName") + ":"
                    + getServletConfig().getInitParameter("dbPort") + "/"
                    + getServletConfig().getInitParameter("databaseName"),
                    getServletConfig().getInitParameter("dbUserName"),
                    getServletConfig().getInitParameter("dbPassword")
            );

        } catch (Exception e) {
            LOGGER.severe("Init error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect(request.getContextPath() + "/ErrorPages/error_session.jsp");
            return;
        }

        String role = (String) session.getAttribute("role");
        String email = (String) session.getAttribute("email");

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        LocalDateTime now = LocalDateTime.now();

        String fileName = (isAdmin ? "Admin_Report_"
                : "Guest_Report_" + email.split("@")[0] + "_")
                + now.format(TS_FILE) + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        Document document;

        if (isAdmin) {
            document = new Document(PageSize.LETTER, 40, 40, 100, 60);
        } else {
            Rectangle squarePage = new Rectangle(320, 320);
            document = new Document(squarePage, 20, 20, 55, 40);
        }

        try {
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

            writer.setPageEvent(new ReportPageEvent(
                    email,
                    isAdmin ? "ADMIN REPORT" : "GUEST REPORT"
            ));

            document.open();

            Paragraph title = new Paragraph(
                    isAdmin ? ADMIN_TITLE : GUEST_TITLE,
                    FONT_TITLE
            );
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(2f);
            document.add(title);

            Paragraph ts = new Paragraph(
                    "Generated on: " + now.format(TS_DISPLAY),
                    FONT_TIMESTAMP
            );
            ts.setAlignment(Element.ALIGN_CENTER);
            ts.setSpacingAfter(10f);
            document.add(ts);

            if (isAdmin) {
                addAdminBody(document);
            } else {
                addGuestBody(document, email);
            }

            LOGGER.info("Report generated: " + fileName + " by " + email);

        } catch (Exception e) {
            LOGGER.severe("Error generating report: " + e.getMessage());
            throw new ServletException("Report generation failed.");
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private void addAdminBody(Document document) throws Exception {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 50});
        table.setSpacingBefore(5f);

        String[] headers = {"Username", "Role"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_TABLE_HEADER));
            cell.setBackgroundColor(new BaseColor(52, 73, 94));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        String sql = "SELECT Email, UserRole FROM USERS ORDER BY Email";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PdfPCell c1 = new PdfPCell(new Phrase(rs.getString("Email"), FONT_TABLE));
                c1.setPadding(6);

                PdfPCell c2 = new PdfPCell(new Phrase(rs.getString("UserRole"), FONT_TABLE));
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setPadding(6);

                table.addCell(c1);
                table.addCell(c2);
            }
        }

        document.add(table);
    }

    private void addGuestBody(Document document, String email) throws Exception {

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);

        String sql = "SELECT Email, Password FROM USERS WHERE Email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    table.addCell(new PdfPCell(new Phrase("Username:", FONT_LABEL)) {{
                        setBorder(Rectangle.NO_BORDER);
                        setPaddingTop(4);
                    }});

                    table.addCell(new PdfPCell(new Phrase(rs.getString("Email"), FONT_VALUE)) {{
                        setBorder(Rectangle.BOTTOM);
                        setPaddingBottom(10);
                    }});

                    table.addCell(new PdfPCell(new Phrase("Decrypted Password:", FONT_LABEL)) {{
                        setBorder(Rectangle.NO_BORDER);
                        setPaddingTop(6);
                    }});

                    table.addCell(new PdfPCell(new Phrase(
                            decryptPassword(rs.getString("Password")), FONT_VALUE)) {{
                        setBorder(Rectangle.BOTTOM);
                        setPaddingBottom(10);
                    }});
                }
            }
        }

        document.add(table);
    }

    private String decryptPassword(String encrypted) {
        try {
            byte[] keyBytes = secretKey.getBytes("UTF-8");
            byte[] validKey = new byte[16];
            System.arraycopy(keyBytes, 0, validKey, 0,
                    Math.min(keyBytes.length, 16));

            Cipher c = Cipher.getInstance(cipherAlgorithm);
            c.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(validKey, cipherAlgorithm));

            return new String(c.doFinal(
                    Base64.getDecoder().decode(encrypted)), "UTF-8");

        } catch (Exception e) {
            return "Error";
        }
    }

    private static class ReportPageEvent extends PdfPageEventHelper {

        private String owner, type;
        private PdfTemplate total;
        private BaseFont bf;

        public ReportPageEvent(String owner, String type) {
            this.owner = owner;
            this.type = type;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
            try {
                bf = BaseFont.createFont(
                        BaseFont.HELVETICA_OBLIQUE,
                        BaseFont.WINANSI,
                        false
                );
            } catch (Exception ignored) {}
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            PdfContentByte cb = writer.getDirectContent();
            Rectangle page = document.getPageSize();
            float footerY = 25;

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(type,
                            FontFactory.getFont(
                                    FontFactory.HELVETICA_BOLDOBLIQUE, 11)),
                    (page.getLeft() + page.getRight()) / 2,
                    page.getTop() - 30, 0);

            float leftX = document.left();

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("Owner: " + owner,
                            FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8)),
                    leftX, footerY, 0);

            String text = "Page " + writer.getPageNumber() + " of ";
            float textWidth = bf.getWidthPoint(text, 8);
            float startX = page.getRight() - textWidth - 35;

            cb.beginText();
            cb.setFontAndSize(bf, 8);
            cb.setTextMatrix(startX, footerY);
            cb.showText(text);
            cb.endText();

            cb.addTemplate(total, startX + textWidth, footerY);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            total.beginText();
            total.setFontAndSize(bf, 8);
            total.setTextMatrix(0, 0);
            total.showText(String.valueOf(writer.getPageNumber()));
            total.endText();
        }
    }
}





