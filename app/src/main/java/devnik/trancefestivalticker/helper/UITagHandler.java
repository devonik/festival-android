package devnik.trancefestivalticker.helper;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

/**
 * Created by niklas on 27.03.18.
 */

public class UITagHandler implements Html.TagHandler {
    @Override
    public void handleTag(boolean opening, String tag, Editable output,
                          XMLReader xmlReader) {
        if(tag.equals("ul") && !opening) output.append("\n\n");
        if(tag.equals("li") && opening) output.append("\n•\t");
    }
}
