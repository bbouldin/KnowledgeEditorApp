package knowledgeeditorapp

import me.aatma.languagetologic.TextAnalyzer
import me.aatma.library.sapi.Context
import me.aatma.library.sapi.SIndividual
import me.aatma.library.sapi.config.SAPIConfiguration
import me.aatma.library.sapi.jenasclient.Constants
import me.aatma.library.sapi.jenasclient.ContextImpl
import me.aatma.library.sapi.jenasclient.SIndividualImpl
import me.aatma.library.sapi.jenasclient.SObjectImpl
import org.apache.jena.query.ReadWrite


class L2LEditorInterceptor {

    boolean before() {
        SObjectImpl.getDataset().begin(ReadWrite.WRITE);

        String userAboutNs = Constants.ABOX_PART + "User/About/" + "xyz123" + "#";
        Context userAboutCtx = new ContextImpl(userAboutNs);

        String userDiet = Constants.ABOX_PART + "User/Diet/" + "xyz123" + "#";
        Context userDietCtx = new ContextImpl(userDiet);

        SIndividual vijay = new SIndividualImpl(userAboutNs + "VijayRaj", userAboutCtx);

        // Operations are always transcripted, if the transcript location is specified
        // currently hardcoded
        SAPIConfiguration.setCurrentUserContext(userDietCtx);
        SAPIConfiguration.setCurrentUser(vijay);

        if (session["analyzed_texts"] == null) {
            session["analyzed_texts"] = new ArrayList<TextAnalyzer>();
        }

        true
    }

    boolean after() {
        true
    }

    void afterView() {
        // no-op
        SObjectImpl.getDataset().commit();
        SObjectImpl.getDataset().end();
    }
}
