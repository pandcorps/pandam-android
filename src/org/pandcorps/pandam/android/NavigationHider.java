/*
Copyright (c) 2009-2014, Andrew M. Martin
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Pandam nor the names of its contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.pandcorps.pandam.android;

import android.view.*;

public final class NavigationHider implements WindowInitializer {
	private final static int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 2; // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // API level 14
	private final static int SYSTEM_UI_FLAG_FULLSCREEN = 4; // View.SYSTEM_UI_FLAG_FULLSCREEN; // API level 16
	private final static int SYSTEM_UI_FLAG_IMMERSIVE = 2048; // View.SYSTEM_UI_FLAG_IMMERSIVE; // API level 19
	private final static int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 4096; // View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // API level 19
	
	@Override
	public final void init(final Window window) {
		window.getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_FULLSCREEN
				| SYSTEM_UI_FLAG_IMMERSIVE | SYSTEM_UI_FLAG_IMMERSIVE_STICKY); // API level 11
	}
}