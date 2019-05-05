package devnik.trancefestivalticker.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Objects;

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
            String folderToSave = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                    !Environment.isExternalStorageRemovable() ? Objects.requireNonNull(context.getExternalCacheDir()).getPath() :
                    context.getCacheDir().getPath();
            folderToSave += "/pdfThumbs";
            return BitmapUtils.saveThumbnail(bmp, FilenameUtils.getBaseName(pdfUri.getPath()), new File(folderToSave));
        } catch(Exception e) {
            //todo with exception
        }
        return null;
    }
    public static PdfDocument.Meta getPdfMetas(PdfiumCore core, PdfDocument doc) {
        return core.getDocumentMeta(doc);
    }
}
