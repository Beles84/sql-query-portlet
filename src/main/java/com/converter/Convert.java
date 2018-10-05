package com.converter;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Font;
import jxl.write.*;
import jxl.write.Number;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Convert {
    /* Получить заголовки */
    public static String[] getHeaders(JSONArray json){
        String[] result=null;

        Set<String> ff = new HashSet<String>();
        for(int i=1; i<json.length(); i++){
            JSONObject element = (JSONObject) json.get(i);
            Iterator<String> it = element.keys();
            while (it.hasNext()){
                ff.add(it.next());
            }
        }

        int i=0;
        result = new String[ff.size()];
        Iterator<String> it = ff.iterator();
        while (it.hasNext()){
            result[i] = it.next();
            i++;
        }
        return result;
    }

    /*Создать таблицу Excel*/
    public static byte[] JSONtoByteEXL(JSONArray json) throws WriteException, IOException {

        WritableWorkbook workbook = null;
        String formatString = "dd.MM.yyyy";
        DateFormat dfShort = new SimpleDateFormat(formatString);
        DateFormat dfFull = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        WritableFont times12font = new WritableFont(WritableFont.TIMES, 10);
        WritableFont times12fontBold = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD);

        WritableCellFormat infoFormat = new WritableCellFormat(times12font);

        WritableCellFormat headerFormat = new WritableCellFormat(times12fontBold);
        headerFormat.setAlignment(jxl.format.Alignment.CENTRE);
        headerFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

        WritableCellFormat dataFormat = new WritableCellFormat(times12font);
        dataFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        dataFormat.setWrap(true);
        dataFormat.setShrinkToFit(true);

        WritableCellFormat dateFormat = new WritableCellFormat(new jxl.write.DateFormat(formatString));
        dateFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

        CellView cellView = new CellView();
        cellView.setSize(5000);

        ByteArrayOutputStream baos = null;
        baos = new ByteArrayOutputStream();
        workbook = Workbook.createWorkbook(baos);
        WritableSheet sheet = workbook.createSheet("Sheet1", 0);

        int rowCounter = 0;

        String headers[] = getHeaders(json);
        int headLenght[] = new int[json.length()];
        int row_line=1;
        for (int j = 0; j < headers.length; j++) {
            headLenght[j] = headers.length;
            sheet.addCell(new Label(j, rowCounter, headers[j], headerFormat));
        }



        Integer maxheaders[][] = new Integer[json.length()][headers.length];

        rowCounter++;
        for(int i=0; i < json.length(); i++){
            JSONObject line_object = json.getJSONObject(i);


            for(int j=0; j< headers.length; j++){
                String key = headers[j];
                try {
                    Object value = line_object.get(key);
                    sheet.addCell(new jxl.write.Label(j, rowCounter+i, value.toString(), dataFormat));
                    maxheaders[i][j] = value.toString().length();
                } catch (JSONException e){
                    sheet.addCell(new jxl.write.Label(j, rowCounter+i, "", dataFormat));
                    maxheaders[i][j] = 0;
                }

            }
        }
        Integer max[] = new Integer[headers.length];
        for (int j = 0; j < headers.length; j++) {
            int maxValue = 0;
            for (int i = 0; i < json.length(); i++) {
                if (maxheaders[i][j] > maxValue) {
                    maxValue = maxheaders[i][j];
                }
                max[j] = maxValue+4;
            }

        }
        for (int j = 0; j < headers.length; j++){
            int value=0;
            if (max[j]>headLenght[j]) {
                value = max[j];
            } else
                value = headLenght[j];
            sheet.setColumnView(j, value);
            //System.out.println(j+":"+value);
        }

        workbook.write();
        workbook.close();


        return baos.toByteArray();
    }
}
