package de.tobias.playpad.action.mapper;

/**
 * Dieses Interface ermöglicht einem Mapper Gerät (nicht einem speziellen Mapping mit einer Action) sich einzurichten und wieder zu clearen.
 *
 * @author tobias
 */
public interface MapperConnectFeedbackable {

	void clearFeedbackType();

	void initFeedbackType();
}
