/* GeneratorListener: callbacks for GeneratorStream.
   $Id: GeneratorListener.java,v 1.1 2008/10/20 04:50:13 rlexvold Exp $

   Copyright (C) 2003  Casey Marshall <rsdio@metastatic.org>

   This file is a part of Jarsync.

   Jarsync is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at
   your option) any later version.

   Jarsync is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Jarsync; if not, write to the

      Free Software Foundation, Inc.,
      59 Temple Place, Suite 330,
      Boston, MA  02111-1307
      USA

   Linking Jarsync statically or dynamically with other modules is
   making a combined work based on Jarsync.  Thus, the terms and
   conditions of the GNU General Public License cover the whole
   combination.

   As a special exception, the copyright holders of Jarsync give you
   permission to link Jarsync with independent modules to produce an
   executable, regardless of the license terms of these independent
   modules, and to copy and distribute the resulting executable under
   terms of your choice, provided that you also meet, for each linked
   independent module, the terms and conditions of the license of that
   module.  An independent module is a module which is not derived from
   or based on Jarsync.  If you modify Jarsync, you may extend this
   exception to your version of it, but you are not obligated to do so.
   If you do not wish to do so, delete this exception statement from
   your version.  */


package org.metastatic.rsync;

import java.util.EventListener;

/**
 * Standard interface for the checksum generator callback, called by
 * {@link GeneratorStream} when new checksum pairs are ready.
 *
 * @version $Revision: 1.1 $
 */
public interface GeneratorListener extends EventListener {

   /**
    * Update with a single, new checksum pair.
    *
    * @param event The event containing the next checksum pair.
    */
   void update(GeneratorEvent event) throws ListenerException;
}
