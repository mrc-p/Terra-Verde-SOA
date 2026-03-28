package br.com.terraverde.soa.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import br.com.terraverde.soa.model.Parceiro;
import br.com.terraverde.soa.repository.ParceiroRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RelatorioService {

    @Autowired
    private ParceiroRepository parceiroRepository;

    /**
     * Gera um relatório PDF completo de todos os parceiros com resumos estatísticos.
     * Este serviço é independente e pode ser consumido por SOAP, REST ou outras camadas.
     * @return Array de bytes do documento PDF.
     */
    public byte[] gerarRelatorioPdf() throws DocumentException {
        // 1. Coleta de dados através do Repository
        List<Parceiro> parceiros = parceiroRepository.findAll();
        
        long totalClientes = parceiroRepository.countByTipo("CLIENTE");
        long totalFornecedores = parceiroRepository.countByTipo("FORNECEDOR");
        long totalParceiros = parceiros.size();
        
        List<Parceiro> top10Parceiros = parceiroRepository.findTop10ByOrderByDataHoraCadastroDesc();
        LocalDateTime umMesAtras = LocalDateTime.now().minusDays(30);
        List<Parceiro> parceirosUltimoMes = parceiroRepository.findByDataHoraCadastroAfter(umMesAtras);
        
        // 2. Configuração do Documento PDF (iText)
        Document document = new Document(PageSize.A4.rotate()); 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Configuração de Estilos e Fontes
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
            Font sectionTitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
            Font dataSummaryFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE);
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Cabeçalho do Documento
            document.add(new Paragraph("RELATÓRIO COMPLETO DE PARCEIROS (TERRA VERDE)", titleFont));
            document.add(new Paragraph("Data de Geração: " + LocalDateTime.now().format(formatter) + "\n"));
            
            // Bloco de Resumo Estatístico
            document.add(new Paragraph("Total de Parceiros Cadastrados: " + totalParceiros, dataSummaryFont));
            document.add(new Paragraph("Total de Clientes: " + totalClientes, dataSummaryFont));
            document.add(new Paragraph("Total de Fornecedores: " + totalFornecedores + "\n\n", dataSummaryFont));
            
            // 1. TABELA PRINCIPAL (Relatório Completo)
            document.add(new Paragraph("1. Relatório Completo", sectionTitleFont));
            PdfPTable table = new PdfPTable(8); 
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            float[] widths = {0.5f, 2f, 1f, 1.5f, 2.5f, 1f, 1f, 2f}; 
            table.setWidths(widths);

            addTableHeader(table, headerFont, "ID");
            addTableHeader(table, headerFont, "NOME/RAZÃO SOCIAL");
            addTableHeader(table, headerFont, "DOCUMENTO");
            addTableHeader(table, headerFont, "TIPO"); 
            addTableHeader(table, headerFont, "EMAIL");
            addTableHeader(table, headerFont, "TELEFONE");
            addTableHeader(table, headerFont, "CADASTRO");
            addTableHeader(table, headerFont, "OBSERVAÇÕES");

            for (Parceiro parceiro : parceiros) {
                String dataCadastro = parceiro.getDataHoraCadastro() != null 
                    ? parceiro.getDataHoraCadastro().format(formatter) : "";

                table.addCell(createCell(parceiro.getId().toString(), dataFont, Element.ALIGN_CENTER));
                table.addCell(createCell(parceiro.getNomeOuRazaoSocial(), dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(parceiro.getDocumento(), dataFont, Element.ALIGN_CENTER));
                table.addCell(createCell(parceiro.getTipo(), dataFont, Element.ALIGN_CENTER)); 
                table.addCell(createCell(parceiro.getEmail(), dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(parceiro.getTelefone(), dataFont, Element.ALIGN_CENTER));
                table.addCell(createCell(dataCadastro, dataFont, Element.ALIGN_CENTER));
                table.addCell(createCell(parceiro.getObservacoes(), dataFont, Element.ALIGN_LEFT));
            }

            document.add(table);
            document.add(new Paragraph("\n\n")); 
            
            // 2. TABELA DOS 10 ÚLTIMOS CADASTROS
            document.add(new Paragraph("2. Resumo: 10 Últimos Parceiros Cadastrados", sectionTitleFont));
            float[] summaryWidths = {0.5f, 3f, 1.5f, 1f, 1.5f}; 
            document.add(createSummaryTable(top10Parceiros, headerFont, dataFont, formatter, summaryWidths));

            document.add(new Paragraph("\n\n")); 
            
            // 3. TABELA DE CADASTROS NO ÚLTIMO MÊS
            String dataMes = umMesAtras.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            document.add(new Paragraph("3. Resumo: Parceiros Cadastrados no Último Mês (" + dataMes + " - Hoje)", sectionTitleFont));
            document.add(createSummaryTable(parceirosUltimoMes, headerFont, dataFont, formatter, summaryWidths));
            
            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            throw new DocumentException("Erro ao processar estrutura do PDF: " + e.getMessage());
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }
    
    // Métodos Auxiliares de Formatação de Tabelas
    private PdfPTable createSummaryTable(List<Parceiro> parceiros, Font headerFont, Font dataFont, DateTimeFormatter formatter, float[] widths) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setWidths(widths);

        addTableHeader(table, headerFont, "ID");
        addTableHeader(table, headerFont, "NOME/RAZÃO SOCIAL");
        addTableHeader(table, headerFont, "DOCUMENTO");
        addTableHeader(table, headerFont, "TIPO");
        addTableHeader(table, headerFont, "CADASTRO");

        for (Parceiro parceiro : parceiros) {
            String dataCadastro = parceiro.getDataHoraCadastro() != null 
                ? parceiro.getDataHoraCadastro().format(formatter) : "";

            table.addCell(createCell(parceiro.getId().toString(), dataFont, Element.ALIGN_CENTER));
            table.addCell(createCell(parceiro.getNomeOuRazaoSocial(), dataFont, Element.ALIGN_LEFT));
            table.addCell(createCell(parceiro.getDocumento(), dataFont, Element.ALIGN_CENTER));
            table.addCell(createCell(parceiro.getTipo(), dataFont, Element.ALIGN_CENTER)); 
            table.addCell(createCell(dataCadastro, dataFont, Element.ALIGN_CENTER));
        }
        return table;
    }
    
    private void addTableHeader(PdfPTable table, Font headerFont, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(52, 73, 94)); // Cor Dark Blue-Grey
        cell.setPadding(5);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }
    
    private PdfPCell createCell(String content, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        cell.setBorderWidth(0.5f);
        return cell;
    }
}