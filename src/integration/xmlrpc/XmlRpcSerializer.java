/*
    Copyright (c) 2007 Redstone Handelsbolag

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

package integration.xmlrpc;

import integration.xmlrpc.serializers.BooleanArraySerializer;
import integration.xmlrpc.serializers.CollectionSerializer;
import integration.xmlrpc.serializers.DoubleArraySerializer;
import integration.xmlrpc.serializers.FloatArraySerializer;
import integration.xmlrpc.serializers.IntArraySerializer;
import integration.xmlrpc.serializers.IntrospectingSerializer;
import integration.xmlrpc.serializers.ListSerializer;
import integration.xmlrpc.serializers.LongArraySerializer;
import integration.xmlrpc.serializers.LongPrimitiveSerializer;
import integration.xmlrpc.serializers.LongWrapperSerializer;
import integration.xmlrpc.serializers.MapSerializer;
import integration.xmlrpc.serializers.ObjectArraySerializer;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *  The XmlRpcSerializer class converts Java objects to their XML-RPC counterparts
 *  according to the XML-RPC specification. It inherently supports basic object
 *  types like String, Integer, Double, Float, Boolean, Date, and byte arrays. For other
 *  types of objects, custom serializers need to be registered. The Redstone XML-RPC
 *  library comes with a set of useful serializers for collections and other types
 *  of objects. @see the redstone.xmlrpc.serializers .
 *  
 *  TODO Change synchronization of global dateFormatter to prevent bottleneck.
 *
 *  @author Greger Olsson
 */

public class XmlRpcSerializer
{
    /**
     *  Constructor adding all core custom serializers.
     */

    public XmlRpcSerializer()
    {
        this( true );
    }    
    

    /**
     *  Constructor that may add all the custom serializers in the library
     *  (which is almost always what you want).
     *  
     *  @param addCustomSerializers Indicates if the core custom serializers should be added.
     */

    public XmlRpcSerializer( boolean addCustomSerializers )
    {
        if ( addCustomSerializers )
        {
            customSerializers.add( new LongPrimitiveSerializer() );
            customSerializers.add( new LongWrapperSerializer() );
            customSerializers.add( new MapSerializer() );
            customSerializers.add( new ListSerializer() );
            customSerializers.add( new CollectionSerializer() );
            customSerializers.add( new ObjectArraySerializer() );
            customSerializers.add( new IntArraySerializer() );
            customSerializers.add( new FloatArraySerializer() );
            customSerializers.add( new LongArraySerializer() );
            customSerializers.add( new DoubleArraySerializer() );
            customSerializers.add( new BooleanArraySerializer() );
            customSerializers.add( new IntrospectingSerializer() );
        }
    }
    
    
    /**
     *  <describe>
     * 
     *  @param writer
     *  @throws IOException
     */

    public void writeEnvelopeHeader( Object value, Writer writer ) throws IOException
    {
        writer.write( "<?xml version=\"1.0\" encoding=\"" );
        writer.write( XmlRpcMessages.get().getString( "XmlRpcServlet.Encoding" ) );
        writer.write( "\"?><methodResponse><params><param>" );
    }


    /**
     *  <describe>
     * 
     *  @param value
     *  @param writer
     * @throws IOException 
     */

    public void writeEnvelopeFooter( Object value, Writer writer ) throws IOException
    {
        if ( value != null )
        {
            writer.write( "</param></params></methodResponse>" );
        }
        else
        {
            writer.write( "<value><string>void</string></value>" +
                          "</param></params></methodResponse>" );
        }
    }
    
    
    /**
     *  <describe>
     * 
     *  @param message
     *  @param writer
     * @throws IOException 
     */

    public void writeError( String message, Writer writer ) throws IOException
    {
        writer.write( "<?xml version=\"1.0\" encoding=\"" );
        writer.write( XmlRpcMessages.get().getString( "XmlRpcServlet.Encoding" ) );
        writer.write( "\"?>" );
        writer.write( "<methodResponse><fault><value><struct>" );
        writer.write( "<member><name>faultCode</name><value><int>-1</int></value>" );
        writer.write( "</member><member><name>faultString</name><value><string>" );
        writer.write( message );
        writer.write( "</string></value></member></struct></value></fault></methodResponse>" );
    }
    
    
    /**
     *  Converts the supplied Java object to its XML-RPC counterpart according to
     *  the XML-RPC specification.
     */

    public void serialize(
        Object value,
        Writer writer )
        throws XmlRpcException, IOException
    {
        writer.write( "<value>" );

        if ( value instanceof String || value instanceof Character )
        {
            writer.write( "<string>" );

            String string = value.toString();
            int length = string.length();

            for ( int i = 0; i < length; ++i )
            {
                char c = string.charAt( i );

                switch( c )
                {
                    case '<' :
                        writer.write( "&lt;" );
                        break;

                    case '&' :
                        writer.write( "&amp;" );
                        break;

                    default :
                        writer.write (c);
                }
            }

            writer.write( "</string>" );
        }
        else if ( value instanceof Integer ||
                  value instanceof Short   ||
                  value instanceof Byte )
        {
            writer.write( "<i4>" );
            writer.write( value.toString() );
            writer.write( "</i4>" );
        }
        else if ( value instanceof Double ||
                  value instanceof Float )
        {
            writer.write( "<double>" );
            writer.write( value.toString() );
            writer.write( "</double>" );
        }
        else if ( value instanceof Boolean )
        {
            writer.write( "<boolean>" );
            writer.write( ( ( Boolean ) value ).booleanValue() == true ? "1" : "0" );
            writer.write( "</boolean>" );
        }
        else if ( value instanceof java.util.Calendar )
        {
            writer.write( "<dateTime.iso8601>" );

            synchronized( dateFormatter )
            {
                writer.write( dateFormatter.format( ( ( Calendar ) value ).getTime() ) );
            }

            writer.write( "</dateTime.iso8601>" );
        }
        else if ( value instanceof java.util.Date )
        {
            writer.write( "<dateTime.iso8601>" );

            synchronized( dateFormatter )
            {
                writer.write( dateFormatter.format( ( Date ) value ) );
            }

            writer.write( "</dateTime.iso8601>" );
        }
        else if ( value instanceof byte[] )
        {
            writer.write( "<base64>" );
            writer.write( Base64.encode( ( byte[] ) value ) );
            writer.write( "</base64>" );
        }
        else
        {
            // Value was not of basic type, see if there's a custom serializer
            // registered for it.

            for ( int i = 0; i < customSerializers.size(); ++i )
            {
                XmlRpcCustomSerializer serializer = ( XmlRpcCustomSerializer ) customSerializers.get( i );
                
                if ( serializer.getSupportedClass().isInstance( value ) )
                {
                    serializer.serialize( value, writer, this );
                    writer.write( "</value>" );
                    return;
                }
            }

            throw new XmlRpcException(
                XmlRpcMessages.get().getString( "XmlRpcSerializer.UnsupportedType" ) + value.getClass() );
        }

        writer.write( "</value>" );
    }


    /**
     *  Registers a custom serializer. The serializer is placed the list of serializers
     *  before more general serializers from the same inheritance tree. That is, adding
     *  a serializer supporting serialization of java.util.Vector will be placed before
     *  a serializer for java.util.Collection. In other words, when serializing an
     *  object of type Vector, the java.util.Vector serializer will override a
     *  more general java.util.Collection serializer.
     *
     *  @value customSerializer The serializer to extend the original serializer with.
     */

    public void addCustomSerializer(
        XmlRpcCustomSerializer customSerializer )
    {
        Class supportedClass = customSerializer.getSupportedClass();

        for ( int i = 0; i < customSerializers.size(); ++i )
        {
            XmlRpcCustomSerializer customSerializerEntry = ( XmlRpcCustomSerializer ) customSerializers.get( i );

            // Does the supplied serializer support a subclass or sub-interface of
            // the serializer at the current element. If so, the supplied serializer
            // should end up in front of the current entry.

            if ( customSerializerEntry.getSupportedClass().isAssignableFrom( supportedClass ) )
            {
                customSerializers.add( i, customSerializer );
                return;
            }
        }

        // The serializer does not support classes that inherit from previously
        // registered serializer classes. It may be put at the end of the list.

        customSerializers.add( customSerializer );
    }


    /**
     *  Unregisters a previously registered custom serializer.
     *
     *  @value customSerializer The serializer to unregister.
     */

    public void removeCustomSerializer(
        XmlRpcCustomSerializer customSerializer )
    {
        customSerializers.remove( customSerializer );
    }


    /** The list of currently registered custom serializers */
    protected List/*<XmlRpcCustomSerializer>*/ customSerializers = new ArrayList();
    
    /** Date formatter shared by all XmlRpcValues */
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyyMMdd'T'HH:mm:ss" );
}
