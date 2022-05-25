import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/*
 * @test
 * @bug 8012675
 * @library /java/awt/regtesthelpers
 * @build PassFailJFrame
 * @summary Tests if HTML script tags are unsupported
 * @run main/manual HtmlTagParserTest
 */

public class HtmlTagParserTest {
    private static String instructionsText = "Pass if you can see the " +
            "script tag and its contents as text in JTextFields and " +
            "JTextAreas in the frame. Fail if this is not shown.";

    private static final String htmlText = "<!DOCTYPE html> <html> <head> " +
            "<title>Title</title> </head> <body><script>function foo() " +
            "{alert('Alert generated by script tag function!');}</script>" +
            "This is a test. <input onclick=\"foo()\" type=\"button\" " +
            "value=\"Click Me\" id=\"myButton1\"></input> </body> </html>";

    private static JFrame frame;
    private static JEditorPane jep;
    private static Path testDir;

    public static void createAndShowGUI() throws InterruptedException,
            InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            jep = new JEditorPane();
            JScrollPane scroll = new JScrollPane(jep);
            Dimension scrollSize = new Dimension(500, 300);
            scroll.setPreferredSize(scrollSize);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            frame = new JFrame();
            frame.getContentPane().add(scroll);
            frame.setVisible(true);
            frame.pack();
            frame.setLocationRelativeTo(null);

            PassFailJFrame.addTestFrame(frame);
            PassFailJFrame.positionTestFrame(frame,
                    PassFailJFrame.Position.HORIZONTAL);
        });
    }

    public static void main(String args[]) throws InterruptedException,
            InvocationTargetException, IOException {

        testDir = Path.of(System.getProperty("test.src", "."));
        File f = new File(testDir + "/tagParserTest.html");

        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(htmlText);
        bw.close();

        URL page = HtmlTagParserTest.class.getResource("tagParserTest.html");

        PassFailJFrame pfjFrame = new PassFailJFrame("JScrollPane "
                + "Test Instructions", instructionsText +
                "\n\nHTML Used:\n" + htmlText, 5);

        createAndShowGUI();

        jep.setPage(page);

        pfjFrame.awaitAndCheck();
    }
}
