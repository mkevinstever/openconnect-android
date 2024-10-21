package app.openconnect.core;

import android.content.Context;
import android.text.TextUtils;

import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import app.openconnect.R;
import app.openconnect.VpnProfile;

/**
 * Utility class for handling X.509 certificates.
 */
public class X509Utils {

    /**
     * Get a Certificate from a file or inline string.
     *
     * @param certfilename The certificate file path or inline certificate string.
     * @return The X509Certificate.
     * @throws FileNotFoundException If the file is not found.
     * @throws CertificateException If there is an issue generating the certificate.
     */
    public static Certificate getCertificateFromFile(String certfilename) throws FileNotFoundException, CertificateException {
        CertificateFactory certFact = CertificateFactory.getInstance("X.509");
        InputStream inStream;

        if (certfilename.startsWith(VpnProfile.INLINE_TAG)) {
            int subIndex = certfilename.indexOf("-----BEGIN CERTIFICATE-----");
            subIndex = Math.max(0, subIndex);
            inStream = new ByteArrayInputStream(certfilename.substring(subIndex).getBytes());
        } else {
            inStream = new FileInputStream(certfilename);
        }

        return certFact.generateCertificate(inStream);
    }

    /**
     * Read a PEM object from a file or inline string.
     *
     * @param keyfilename The key file path or inline key string.
     * @return The PemObject.
     * @throws IOException If there is an issue reading the file.
     */
    public static PemObject readPemObjectFromFile(String keyfilename) throws IOException {
        Reader inStream;

        if (keyfilename.startsWith(VpnProfile.INLINE_TAG)) {
            inStream = new StringReader(keyfilename.replace(VpnProfile.INLINE_TAG, ""));
        } else {
            inStream = new FileReader(new File(keyfilename));
        }

        try (PemReader pr = new PemReader(inStream)) {
            return pr.readPemObject();
        }
    }

    /**
     * Get a friendly name for the certificate from a file.
     *
     * @param c       The context for resources.
     * @param filename The certificate filename.
     * @return The friendly name or an error message.
     */
    public static String getCertificateFriendlyName(Context c, String filename) {
        if (!TextUtils.isEmpty(filename)) {
            try {
                X509Certificate cert = (X509Certificate) getCertificateFromFile(filename);
                return getCertificateFriendlyName(cert);
            } catch (Exception e) {
                OpenVPN.logError("Could not read certificate: " + e.getLocalizedMessage());
            }
        }
        return c.getString(R.string.cannotparsecert);
    }

    /**
     * Get the friendly name from an X509Certificate.
     *
     * @param cert The X509Certificate.
     * @return The friendly name of the certificate.
     */
    public static String getCertificateFriendlyName(X509Certificate cert) {
        X500Principal principal = cert.getSubjectX500Principal();
        String friendlyName = principal.getName();

        // Fallback to principal name if no other friendly name is found.
        String[] parts = friendlyName.split(",");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("1.2.840.113549.1.9.1=#16")) {
                parts[i] = "email=" + ia5decode(part.replace("1.2.840.113549.1.9.1=#16", ""));
            }
        }
        return TextUtils.join(",", parts);
    }

    /**
     * Check if a character is a printable character.
     *
     * @param c The character to check.
     * @return True if the character is printable, false otherwise.
     */
    public static boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) && block != null && block != Character.UnicodeBlock.SPECIALS;
    }

    /**
     * Decode an IA5 string.
     *
     * @param ia5string The IA5 string to decode.
     * @return The decoded string.
     */
    private static String ia5decode(String ia5string) {
        StringBuilder d = new StringBuilder();
        for (int i = 1; i < ia5string.length(); i += 2) {
            String hexstr = ia5string.substring(i - 1, i + 1);
            char c = (char) Integer.parseInt(hexstr, 16);
            if (isPrintableChar(c)) {
                d.append(c);
            } else if (i == 1 && (c == 0x12 || c == 0x1b)) {
                // ignore
            } else {
                d.append("\\x").append(hexstr);
            }
        }
        return d.toString();
    }
}