package vn.mavn.patientservice.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.mavn.patientservice.dto.MedicalRecordDto;
import vn.mavn.patientservice.dto.MedicalRecordDto.DiseaseForMedicalRecordDto;
import vn.mavn.patientservice.dto.qobject.QueryMedicalRecordDto;
import vn.mavn.patientservice.service.MedicalRecordService;
import vn.mavn.patientservice.service.ReportService;

/**
 * Created by TaiND on 2020-02-26.
 **/
@Service
@Transactional
public class ReportServiceImpl implements ReportService {

  private static String[] columns = {"Ngày tư vấn", "Mã nhân viên tư vấn", "Loại bệnh", "Bệnh nhân",
      "Tuổi", "Địa chỉ", "Số điện thoại", "Nguồn quảng cáo", "Tình trạng bệnh", "Tình trạng tư vấn",
      "Ghi chú", "Số Zalo", "Liên hệ khác", "Ngày khám", "Phòng khám", "Lần khám", "Số thang",
      "Loại thuốc", "Bài thuốc", "Tổng tiền mặt", "Chuyển khoản", "CoD", "Ghi chú"};

  @Autowired
  private MedicalRecordService medicalRecordService;

  @Override
  public void exportReport(QueryMedicalRecordDto queryMedicalRecordDto,
      HttpServletResponse httpServletResponse) throws IOException {
    long t1 = System.currentTimeMillis();

    // region prepare data
    List<MedicalRecordDto> medicalRecords = medicalRecordService
        .findAllForReport(queryMedicalRecordDto);

    long t2 = System.currentTimeMillis();
    System.out.println("Fetching data: " + (t2 - t1));

    Set<DiseaseForMedicalRecordDto> diseaseForMedicalRecords = medicalRecords.stream()
        .map(MedicalRecordDto::getDiseaseDto).collect(Collectors.toSet());

    // Ten vi thuoc
    Set<String> medicineHeaders = new HashSet<>();
    diseaseForMedicalRecords.forEach(diseaseForMedicalRecordDto -> {
      if ((diseaseForMedicalRecordDto != null) && !CollectionUtils
          .isEmpty(diseaseForMedicalRecordDto.getMedicines())) {
        diseaseForMedicalRecordDto.getMedicines().forEach(medicineDto -> {
          medicineHeaders.add(medicineDto.getName());
        });
      }
    });

    long t3 = System.currentTimeMillis();
    System.out.println("Building header text: " + (t3 - t2));

    // endregion

    // region build template for sheet
    // Create a Workbook
    Workbook workbook = new XSSFWorkbook();

    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.BLACK.getIndex());

    // region create cell styles to format
    /* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
    CreationHelper createHelper = workbook.getCreationHelper();

    // Create Cell Style for formatting Date
    CellStyle dateCellStyle = workbook.createCellStyle();
    dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

    CellStyle currency = workbook.createCellStyle();
    DataFormat currencyFormatter = workbook.createDataFormat();
    currency.setDataFormat(currencyFormatter.getFormat("#,##0.0"));

    CellStyle phoneNumberStyle = workbook.createCellStyle();
    DataFormat phoneFormatter = workbook.createDataFormat();
    phoneNumberStyle.setDataFormat(phoneFormatter.getFormat("(###) ###-####"));

    // Create a CellStyle with the font
    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);
    headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
    headerCellStyle.setFillPattern(FillPatternType.FINE_DOTS);
    //endregion

    // Create a Sheet
    Sheet sheet = workbook.createSheet("Data");

    // Create a header Row
    Row headerRow = sheet.createRow(0);

    // Create column headers
    for (int i = 0; i < columns.length; i++) {
      String title = columns[i];
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(title);
      cell.setCellStyle(headerCellStyle);
      sheet.autoSizeColumn(i);
    }

    int nextExtraColumn = columns.length;
    // Add ten vi thuoc vao header
    Map<String, Integer> mapHeaderColumnIndex = new HashMap<>();
    for (String medicineHeader : medicineHeaders) {
      Cell cell = headerRow.createCell(nextExtraColumn);
      cell.setCellValue(medicineHeader);
      cell.setCellStyle(headerCellStyle);
      sheet.autoSizeColumn(nextExtraColumn);
      mapHeaderColumnIndex.put(medicineHeader, nextExtraColumn);
      nextExtraColumn++;
    }
    // endregion

    long t4 = System.currentTimeMillis();
    System.out.println("Built cell header: " + (t4 - t3));

    // region fill data to spreadsheet
    // Create Other rows and cells with employees data
    int rowNum = 1;
    for (MedicalRecordDto medicalRecord : medicalRecords) {
      Row row = sheet.createRow(rowNum++);

      int cellNum = 0;
      // Ngay tu van
      Cell advisoryDateCell = row.createCell(cellNum);
      advisoryDateCell.setCellValue(medicalRecord.getAdvisoryDate());
      advisoryDateCell.setCellStyle(dateCellStyle);
      ++cellNum;

      // Ma nhan vien tu van
      row.createCell(cellNum)
          .setCellValue(medicalRecord.getUserCode());
      ++cellNum;

      // Loai benh
      row.createCell(cellNum).setCellValue(
          medicalRecord.getDiseaseDto() != null ? medicalRecord.getDiseaseDto().getName() : "");
      ++cellNum;

      // Ten benh nhan
      row.createCell(cellNum).setCellValue(
          medicalRecord.getPatientDto() != null ? medicalRecord.getPatientDto().getName() : "");
      ++cellNum;

      // Tuoi benh nhan
      row.createCell(cellNum, CellType.NUMERIC)
          .setCellValue(
              medicalRecord.getPatientDto() != null ? medicalRecord.getPatientDto().getAge()
                  : Integer.valueOf(0));
      ++cellNum;

      // Dia chi
      row.createCell(cellNum)
          .setCellValue(
              medicalRecord.getPatientDto() != null ? medicalRecord.getPatientDto().getAddress()
                  : "");
      ++cellNum;

      // SDT
      Cell phoneNumber = row.createCell(cellNum);
      phoneNumber.setCellValue(
          medicalRecord.getPatientDto() != null ? medicalRecord.getPatientDto().getPhone()
              : "");
      phoneNumber.setCellStyle(phoneNumberStyle);
      ++cellNum;

      // Nguon QC
      row.createCell(cellNum).setCellValue(
          medicalRecord.getAdvertisingSourceDto() != null ? medicalRecord.getAdvertisingSourceDto()
              .getName() : "");
      ++cellNum;

      // Tinh trang benh
      row.createCell(cellNum).setCellValue(medicalRecord.getDiseaseStatus());
      ++cellNum;

      // Tinh trang tu van
      row.createCell(cellNum).setCellValue(
          medicalRecord.getConsultingStatusDto() != null ? medicalRecord.getConsultingStatusDto()
              .getName() : "");
      ++cellNum;

      // Ghi chu
      row.createCell(cellNum).setCellValue(medicalRecord.getNote());
      ++cellNum;

      // SDT Zalo
      Cell zaloPhone = row.createCell(cellNum);
      zaloPhone.setCellValue(
          medicalRecord.getPatientDto() != null ? medicalRecord.getPatientDto().getZaloPhone()
              : "");
      zaloPhone.setCellStyle(phoneNumberStyle);
      ++cellNum;

      // SDT khac
      Cell otherContact = row.createCell(cellNum);
      otherContact.setCellValue(
          medicalRecord.getPatientDto() != null ? medicalRecord.getPatientDto().getOtherPhone()
              : "");
      otherContact.setCellStyle(phoneNumberStyle);
      ++cellNum;

      // Ngay kham
      Cell examDateCell = row.createCell(cellNum);
      examDateCell.setCellValue(medicalRecord.getExaminationDate());
      examDateCell.setCellStyle(dateCellStyle);
      ++cellNum;

      // Co so kham
      row.createCell(cellNum)
          .setCellValue(
              medicalRecord.getClinicDto() != null ? medicalRecord.getClinicDto().getName() : "");
      ++cellNum;

      // Lan kham
      row.createCell(cellNum).setCellValue(
          medicalRecord.getExaminationTimes() != null ? medicalRecord.getExaminationTimes()
              : Long.valueOf(0));
      ++cellNum;

      // So thang
      row.createCell(cellNum, CellType.NUMERIC).setCellValue(
          medicalRecord.getRemedyAmount() != null ? medicalRecord.getRemedyAmount()
              : Long.valueOf(0));
      ++cellNum;

      // Loai thuoc
      row.createCell(cellNum).setCellValue(medicalRecord.getRemedyType());
      ++cellNum;

      // Bai thuoc
      row.createCell(cellNum).setCellValue(medicalRecord.getRemedies());
      ++cellNum;

      // Tong tien mat
      Cell totalAmount = row.createCell(cellNum, CellType.NUMERIC);
      totalAmount.setCellValue(
          medicalRecord.getTotalAmount() != null ? medicalRecord.getTotalAmount().doubleValue()
              : 0);
      totalAmount.setCellStyle(currency);
      ++cellNum;

      // Chuyen khoan
      Cell transfer = row.createCell(cellNum, CellType.NUMERIC);
      transfer.setCellValue(
          medicalRecord.getTransferAmount() != null ? medicalRecord.getTransferAmount()
              .doubleValue() : 0);
      transfer.setCellStyle(currency);
      ++cellNum;

      // CoD
      Cell cod = row.createCell(cellNum, CellType.NUMERIC);
      cod.setCellValue(
          medicalRecord.getCodAmount() != null ? medicalRecord.getCodAmount().doubleValue()
              : 0);
      cod.setCellStyle(currency);
      ++cellNum;

      // Ghi chu mo rong
      row.createCell(cellNum).setCellValue(medicalRecord.getExtraNote());
      ++cellNum;

      // Nhao so luong vi thuoc
      if (medicalRecord.getDiseaseDto() != null) {
        medicalRecord.getDiseaseDto().getMedicines().forEach(medicineDto -> {
          int cellIndex = mapHeaderColumnIndex.get(medicineDto.getName());
          row.createCell(cellIndex, CellType.NUMERIC).setCellValue(
              medicineDto.getQty() != null ? medicineDto.getQty() : Integer.valueOf(0));
        });
      }
    }
    // endregion

    long t5 = System.currentTimeMillis();
    System.out.println("Built cell value: " + (t5 - t4));

    // Try to determine file's content type
    String pattern = "MM-dd-yyyy-HH-mm-ss";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    String fileName = "Du_lieu_" + simpleDateFormat.format(new Date()) + ".xlsx";

    httpServletResponse
        .setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    httpServletResponse
        .setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    workbook.write(httpServletResponse.getOutputStream());
    // Closing the workbook
    workbook.close();
  }
}
