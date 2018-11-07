/**
 * 
 */
/**
 * @author Clownvin
 *
 */
module com.git.clownvin.simplepacketframework {
	requires transitive com.git.clownvin.simpleserverframework;
	requires com.git.clownvin.util;
	exports com.git.clownvin.simplepacketframework.packet;
	exports com.git.clownvin.simplepacketframework.connection;
}