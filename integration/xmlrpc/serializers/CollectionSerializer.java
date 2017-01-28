/*
    Copyright (c) 2005 Redstone Handelsbolag

    This library is free software; you can redistribute it and/or modify it under the terms
    of the GNU Lesser General Public License as published by the Free Software Foundation;
    either version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License along with this
    library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
    Boston, MA  02111-1307  USA
*/
 package integration.xmlrpc.serializers;


import integration.xmlrpc.XmlRpcCustomSerializer;
import integration.xmlrpc.XmlRpcException;
import integration.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

/**
 *  Serializes java.util.Collections. See also ListSerializer which serializes
 *  java.util.Lists which use direct access instead of iterators.
 * 
 *  @author Greger Olsson
 *  @see ListSerializer
 */

public class CollectionSerializer implements XmlRpcCustomSerializer
{
    /*  (Documentation inherited)
     *  @see redstone.xmlrpc.XmlRpcCustomSerializer#getSupportedClass()
     */
    
    public Class getSupportedClass()
    {
        return Collection.class;
    }


    /*  (Documentation inherited)
     *  @see redstone.xmlrpc.XmlRpcCustomSerializer#serialize(java.lang.Object, java.io.Writer, redstone.xmlrpc.XmlRpcSerializer)
     */
    
    public void serialize(
        Object value,
        Writer writer,
        XmlRpcSerializer builtInSerializer )
        throws XmlRpcException, IOException
    {
        writer.write( "<array><data>" );

        Iterator it = ( ( Collection ) value ).iterator();

        while ( it.hasNext() )
        {
            builtInSerializer.serialize( it.next(), writer );
        }

        writer.write( "</data></array>" );
    }
}