package app.openconnect.core;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import app.openconnect.R;
import app.openconnect.VpnProfile;

public class ProxyDetection {
	static SocketAddress detectProxy(VpnProfile vp) {
		try {
			// Use a external URL for detect proxy.
			URL url = new URL("http://www.amazon.com");
			Proxy proxy = getFirstProxy(url);

			if (proxy == null) {
				return null;
			}

			SocketAddress addr = proxy.address();
			if (addr instanceof InetSocketAddress) {
				return addr;
			}

		} catch (MalformedURLException e) {
			OpenVPN.logError(R.string.getproxy_error, e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			OpenVPN.logError(R.string.getproxy_error, e.getLocalizedMessage());
		}
		return null;
	}

	static Proxy getFirstProxy(URL url) throws URISyntaxException {
		// Enable system proxy settings.
		System.setProperty("java.net.useSystemProxies", "true");

		List<Proxy> proxylist = ProxySelector.getDefault().select(url.toURI());

		if (proxylist != null) {
			for (Proxy proxy : proxylist) {
				SocketAddress addr = proxy.address();
				if (addr != null) {
					// use proxy.type() to check proxy type.
					return proxy;
				}
			}
		}
		return null;
	}
}