package other.writeily.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WrFilesWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new WrFilesWidgetFactory(getApplicationContext(), intent));
    }
}
