package devnik.trancefestivalticker.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class PDFUtils {
    //PdfiumAndroid (https://github.com/barteksc/PdfiumAndroid)
//https://github.com/barteksc/AndroidPdfViewer/issues/49
    public static String generateImageFromPdf(Uri pdfUri, Context context) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        try {
            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(pdfUri, "r");
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);

            pdfiumCore.closeDocument(pdfDocument); // important!
            String pathToFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/tftApp/tickets/pdfThumbs";
            File folderToSave = new File(pathToFolder);
            if(!folderToSave.exists()){
                boolean mkdirs = folderToSave.mkdirs();
            }

            return BitmapUtils.saveThumbnail(bmp, FilenameUtils.getBaseName(pdfUri.getPath()), folderToSave);
        } catch(Exception e) {
            //todo with exception
        }
        return null;
    }
    public static PdfDocument.Meta getPdfMetas(PdfiumCore core, PdfDocument doc) {
        return core.getDocumentMeta(doc);
    }
}
