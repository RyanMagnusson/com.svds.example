package com.svds.example.orientdb;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * @author rmagnus
 *
 */
public class OrientDBEmbeddable {

	public static class User {
		private static final String DEFAULT_RESOURCE_PATH = "*";
		
		private String username;
		private String password;
		private String resources;
		private boolean encrypted = false;
		
		//<user name=\"root\" password=\"ThisIsA_TEST\" resources=\"*\"/>
		@Override
		public String toString() {
			
			final String resourcePath = null == resources ? DEFAULT_RESOURCE_PATH : resources;
			return new StringBuilder(32).append("<user")
					                    .append(" name=\"").append(Objects.toString(getName(),"")).append("\"")
					                    .append(" password=\"").append(Objects.toString(getPassword(),"")).append("\"")
					                    .append(" resources=\"").append(resourcePath).append("\"")
					                    .append(" />")
					                    .toString();
		}
		
		public String getName() { return username; }
		public String getPassword() { return password; }
	}

	public static class Range<T extends Comparable<T>> {
		private T minimum;
		private T maximum;
		
		@Override
		public String toString() {
			if (minimum == null) {
				return null == maximum ? "" : new DecimalFormat("0").format(maximum);
			}
			
			if (null == maximum) { return new DecimalFormat("0").format(minimum); }
			
			T lower = minimum;
			T higher = maximum;
			if (minimum.compareTo(maximum) > 0) {
				lower = maximum;
				higher = minimum;
			}
			
			return MessageFormat.format("{0,number,0}-{1,number,0}",lower,higher);
		}
	}
	
	public enum Protocol { HTTP, BINARY }
	
	public static class Listener {
		private String ipAddress;
		private Range<Integer> ports;
		private Protocol protocol;
		
		@Override
		public String toString() {
			//<listener ip-address=\"0.0.0.0\" port-range=\"2424-2430\" protocol=\"binary\"/>
			
			final String protocolName = null == protocol ? "" : protocol.name().toLowerCase();
			return new StringBuilder(32).append("<listener")
					                    .append(" ip-address=\"").append(Objects.toString(getIpAddress(),"")).append("\"")
					                    .append(" port-range=\"").append(Objects.toString(getPorts(),"")).append("\"")
					                    .append(" protocol=\"").append(protocolName).append("\"")
					                    .append(" />")
					                    .toString();
		}

		/**
		 * @return the ipAddress
		 */
		protected String getIpAddress() {
			return ipAddress;
		}

		/**
		 * @param ipAddress the ipAddress to set
		 */
		protected void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}

		/**
		 * @return the ports
		 */
		protected Range<Integer> getPorts() {
			return ports;
		}

		/**
		 * @param ports the ports to set
		 */
		protected void setPorts(Range<Integer> ports) {
			this.ports = ports;
		}

		/**
		 * @return the protocol
		 */
		protected Protocol getProtocol() {
			return protocol;
		}

		/**
		 * @param protocol the protocol to set
		 */
		protected void setProtocol(Protocol protocol) {
			this.protocol = protocol;
		}
		
	}
	
}
