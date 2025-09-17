package cdti.aidea.earas.config;

import cdti.aidea.earas.contract.Response.BtrDataListResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@Component
public class ExcelExportUtil {

    public static ByteArrayResource generateExcel(List<BtrDataListResponse> dataList) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("BTR Data");

            int rowIdx = 0;
            // Add "S.No" as the first header
            String[] headers = {"Sl.No", "Localbody Name", "Village", "Block", "Re-Survey No", "Owner Name", "Address", "Land Type", "Total Area"};

            // Create header row
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Fill data rows
            int serialNo = 1;
            for (BtrDataListResponse row : dataList) {
                Row dataRow = sheet.createRow(rowIdx++);
                dataRow.createCell(0).setCellValue(serialNo++); // Serial number
                dataRow.createCell(1).setCellValue(row.getLbname());
                dataRow.createCell(2).setCellValue(row.getVillageName());
                dataRow.createCell(3).setCellValue(String.valueOf(row.getBcode()));
                dataRow.createCell(4).setCellValue(String.valueOf(row.getResvno() + "/" + row.getResbdno()));
                dataRow.createCell(5).setCellValue("NA"); // Owner Name
                dataRow.createCell(6).setCellValue("NA"); // Address
                dataRow.createCell(7).setCellValue(row.getLtype());
                dataRow.createCell(8).setCellValue(row.getTotalCent());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

}
