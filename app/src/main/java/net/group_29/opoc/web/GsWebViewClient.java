package net.group_29.opoc.web;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GsWebViewClient extends WebViewClient {
    protected final WeakReference<WebView> m_webView;

    public GsWebViewClient(final WebView webView) {
        m_webView = new WeakReference<>(webView);
    }

    @Override
    public void onPageFinished(final WebView webView, final String url) {
        __onPageFinished_restoreScrollY(webView, url);
        super.onPageFinished(webView, url);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    private final AtomicBoolean m_restoreScrollYEnabled = new AtomicBoolean(false);
    private int m_restoreScrollY = 0;

    /**
     * Activate by {@link GsWebViewClient#setRestoreScrollY(int)}
     *
     * @param webView onPageFinished {@link WebView}
     * @param url     onPageFinished url
     */
    protected void __onPageFinished_restoreScrollY(final WebView webView, final String url) {
        if (m_restoreScrollYEnabled.getAndSet(false)) {
            for (int dt : new int[]{50, 100, 150, 200, 250, 300}) {
                webView.postDelayed(() -> webView.setScrollY(m_restoreScrollY), dt);
            }
        }
    }

    /**
     * Apply vertical scroll position on next page load
     *
     * @param scrollY scroll position from {@link WebView#getScrollY()}
     */
    public void setRestoreScrollY(final int scrollY) {
        m_restoreScrollY = scrollY;
        m_restoreScrollYEnabled.set(scrollY >= 0);
    }

    ////////////////////////////////////////////////////////////////////////////////////
}
