/*
 * Copyright (c) 2012-2016 Augumenta Ltd. All rights reserved.
 *
 * This source code file is furnished under a limited license and may be used or
 * copied only in accordance with the terms of the license. Except as permitted
 * by the license, no part of this source code file may be  reproduced, stored in
 * a retrieval system, or transmitted, in any form or by  any means, electronic,
 * mechanical, recording, or otherwise, without the prior written permission of
 * Augumenta.
 *
 * This source code file contains proprietary information that is protected by
 * copyright. Certain parts of proprietary information is patent protected. The
 * content herein is furnished for informational use only, is subject to change
 * without notice, and should not be construed as a commitment by Augumenta.
 * Augumenta assumes no responsibility or liability for any errors or
 * inaccuracies that may appear in the informational content contained herein.
 * This source code file has not been thoroughly tested under all conditions.
 * Augumenta, therefore, does not guarantee or imply its reliability,
 * serviceability, or function.
 *
 */

package com.example.bryan.odginformar.AR_interaction_utils;

import java.util.ArrayList;
import java.util.List;

public class HandDetectionNotifier {

	public interface Listener {
		void onDetection();
		void onLost();
	}

	private static final List<Listener> listeners = new ArrayList<>();

	public static void addListener(Listener listener) {
		listeners.add(listener);
	}

	public static void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	public static void notifyDetected() {
		for (Listener listener : listeners) {
			listener.onDetection();
		}
	}

	public static void notifyLost() {
		for (Listener listener : listeners) {
			listener.onLost();
		}
	}
}
