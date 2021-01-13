package org.sitemesh.content.tagrules.decorate;

import org.sitemesh.tagprocessor.BasicBlockRule;
import org.sitemesh.tagprocessor.Tag;
import org.sitemesh.SiteMeshContext;
import org.sitemesh.content.ContentProperty;
import org.sitemesh.content.Content;

import java.io.IOException;

/**
 * Replaces tags that look like {@code <sitemesh:write property='foo'/>} with the
 * {@link ContentProperty} being merged into the current document. The body contents of the tag will be
 * used the default value. To get child properties, use a dot notation, e.g. {@code foo.child.grandchild}.
 *
 * @author Joe Walnes
 * @see SiteMeshContext#getContentToMerge()
 */
public class SiteMeshWriteRule extends BasicBlockRule {

    private final SiteMeshContext siteMeshContext;
    private boolean undefined;

    public SiteMeshWriteRule(SiteMeshContext siteMeshContext) {
        this.siteMeshContext = siteMeshContext;
    }

    @Override
    protected Object processStart(Tag tag) throws IOException {
        String propertyPath = tag.getAttributeValue("property", true);
        Content contentToMerge = siteMeshContext.getContentToMerge();
        if (contentToMerge != null) {
            ContentProperty property = getProperty(contentToMerge, propertyPath);
            property.writeValueTo(tagProcessorContext.currentBuffer());
            undefined = property.getValue() == null || property.getValue().trim().length() == 0;
        }
        tagProcessorContext.pushBuffer();
        return null;
    }

    protected ContentProperty getProperty(Content content, String propertyPath) {
        ContentProperty currentProperty = content.getExtractedProperties();
        for (String childPropertyName : propertyPath.split("\\.")) {
            currentProperty = currentProperty.getChild(childPropertyName);
        }
        return currentProperty;
    }

    @Override
    protected void processEnd(Tag tag, Object data) throws IOException {
        CharSequence defaultContents = tagProcessorContext.currentBufferContents();
        tagProcessorContext.popBuffer();
        if (siteMeshContext.getContentToMerge() == null || undefined) {
            tagProcessorContext.currentBuffer().append(defaultContents);
        }
    }
}
