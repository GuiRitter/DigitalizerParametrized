package io.github.guiritter.digitalizer_parametrized;

import static java.lang.System.out;

import java.util.HashMap;
import java.util.Map;

import com.wia.WiaImageIntent;
import com.wia.api.ImageCallback;
import com.wia.api.Wia4j;
import com.wia.api.Wia4jTest;
import com.wia.api.WiaImageFormat;
import com.wia.api.WiaOperationException;

public class DigitalizerParametrizedTest {
	public static void main(String args[]) {
		Wia4j wia4j = new Wia4j();
		Map<Integer, Integer> props = new HashMap<>();
		//scan B&W
		props.put(Wia4j.WIA_IPS_CUR_INTENT, WiaImageIntent.TextIntent.comEnumValue());
		try {
			//scan using the default dialogs from WIA
			// wia4j.scan("C:\\Users\\GuilhermeAlanRitter\\Desktop\\2022-01-13\\test1.png");
			/**
			 * Scans a single page from the feeder
			 * Warning! if there are multiple pages in the feeder, the next page will be stucked in the feeder!
			 */
			// wia4j.scan("C:\\Users\\GuilhermeAlanRitter\\Desktop\\2022-01-13\\test2.png", true, true, WiaImageFormat.wiaFormatPNG, props);
			//scans page from flatbed
			// wia4j.scan("C:\\Users\\GuilhermeAlanRitter\\Desktop\\2022-01-13\\test3.png", false, true, WiaImageFormat.wiaFormatPNG, props);
			// change intent to color
			props.remove(Wia4j.WIA_IPS_CUR_INTENT);
			props.put(Wia4j.WIA_IPS_CUR_INTENT, WiaImageIntent.UnspecifiedIntent.comEnumValue());
			wia4j.scan("C:\\Users\\GuilhermeAlanRitter\\Desktop\\2022-01-13\\test4.png", true, true, WiaImageFormat.wiaFormatPNG, props);
			wia4j.scan("C:\\Users\\GuilhermeAlanRitter\\Desktop\\2022-01-13\\test5.png", false, true, WiaImageFormat.wiaFormatPNG, props);

		} catch (WiaOperationException ex) {
			out.println(Wia4jTest.class.getName());
			out.println(ex);
			out.println();
			out.flush();
		}

		//scan all pages in the feeder
		try {
			wia4j.scan("C:\\Users\\GuilhermeAlanRitter\\Desktop\\test", true, WiaImageFormat.wiaFormatJPEG, props, new ImageCallback<String>() {
				@Override
				public void handleImage(String image) {
					System.out.println(image);
				}
			});
		} catch (WiaOperationException ex) {
			out.println(Wia4jTest.class.getName());
			out.println(ex);
			out.println();
			out.flush();
		}
	}
}
