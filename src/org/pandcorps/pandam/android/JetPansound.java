/*
Copyright (c) 2009-2021, Andrew M. Martin
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

import java.io.*;

import org.pandcorps.core.*;
import org.pandcorps.core.aud.*;
import org.pandcorps.core.io.*;
import org.pandcorps.pandam.*;

import android.media.*;

public final class JetPansound extends Pansound {
	protected static JetPlayer jetPlayer = null;
	private final String loc;
	private final String fileName;
	
	protected JetPansound(final String loc, final InputStream in) {
	    this.loc = loc;
	    try {
	        fileName = AndroidPangine.copyStreamToFile(loc, in).fileName;
	    } catch (final Exception e) {
	        throw Panception.get(e);
	    }
    }
	
	protected JetPansound(final String loc) {
		this.loc = loc;
		fileName = copyResourceToFile();
	}
	
	private final String copyResourceToFile() {
	    if (loc.endsWith("mid")) {
	        return convertMidToJetFile();
	    } else {
	        return copyJetToFile();
	    }
	}
	
	private final String copyJetToFile() {
		try {
			return AndroidPangine.copyResourceToFile(loc).fileName;
		} catch (final Exception e) {
    		throw Panception.get(e);
    	}
	}
	
	private final String convertMidToJetFile() {
	    InputStream in = null;
	    NamedOutputStream out = null;
        try {
            in = Iotil.getResourceInputStream(loc);
            out = AndroidPangine.getCopyOutputStream(toJetLocation(loc));
            Mid2Jet.convert(in, out);
            return out.getName();
        } catch (final Exception e) {
            throw Panception.get(e);
        } finally {
            Iotil.close(out);
            Iotil.close(in);
        }
	}
	
	protected final static String toJetLocation(final String midLocation) {
	    return midLocation.substring(0, midLocation.length() - 3) + "jet";
	}
	
	@Override
	protected final void runMusic() throws Exception {
		AndroidPanaudio.pausedMusic = null;
		run(-1);
	}

	@Override
	protected final void runSound() throws Exception {
		run(0);
	}
	
	private final void run(final int repeatCount) {
		if (jetPlayer == null) {
    		jetPlayer = JetPlayer.getJetPlayer();
    	}
		if (!jetPlayer.loadJetFile(fileName)) {
			copyResourceToFile(); // Maybe cache got cleared, try reloading
			if (!jetPlayer.loadJetFile(fileName)) {
				throw new Panception("Failed to load Jet file " + fileName);
			}
		}
		if (!jetPlayer.queueJetSegment(0, -1, repeatCount, 0, 0, (byte) 0)) {
			throw new Panception("Failed to queue Jet segment");
		}
		if (!jetPlayer.play()) {
			throw new Panception("Failed to play Jet file");
		}
	}
	
	@Override
	protected final void runDestroy() {
	}
}
