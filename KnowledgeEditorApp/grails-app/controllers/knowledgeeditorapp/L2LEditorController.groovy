package knowledgeeditorapp

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.trees.Tree
import groovy.util.logging.Log4j
import me.aatma.languagetologic.BuildKBNLGraph
import me.aatma.languagetologic.TextAnalyzer
import me.aatma.languagetologic.graph.nodes.KBNLNodeCloud
import me.aatma.library.qapi.SQuery
import me.aatma.library.qapi.SResultSet
import me.aatma.library.qapi.jenaclient.SQueryImpl
import me.aatma.library.sapi.Assertion
import me.aatma.library.sapi.Context
import me.aatma.library.sapi.SIndividual
import me.aatma.library.sapi.SRule
import me.aatma.library.sapi.STerm
import me.aatma.library.sapi.Sentence
import me.aatma.library.sapi.config.SAPIConfiguration
import me.aatma.library.sapi.jenasclient.Constants
import me.aatma.library.sapi.jenasclient.ContextImpl
import me.aatma.library.sapi.jenasclient.SIndividualImpl
import me.aatma.library.sapi.jenasclient.SObjectImpl
import me.aatma.library.sapi.jenasclient.SRuleImpl
import me.aatma.library.sapi.jenasclient.SentenceImpl
import me.aatma.library.stanfordcorenlputils.Article
import org.apache.jena.query.ReadWrite

class L2LEditorController {

//    def beforeInterceptor = {
//
//        SObjectImpl.getDataset().begin(ReadWrite.WRITE);
//
//        String userAboutNs = Constants.ABOX_PART + "User/About/" + "xyz123" + "#";
//        Context userAboutCtx = new ContextImpl(userAboutNs);
//
//        String userDiet = Constants.ABOX_PART + "User/Diet/" + "xyz123" + "#";
//        Context userDietCtx = new ContextImpl(userDiet);
//
//        SIndividual vijay = new SIndividualImpl(userAboutNs + "VijayRaj", userAboutCtx);
//
//        // Operations are always transcripted, if the transcript location is specified
//        // currently hardcoded
//        SAPIConfiguration.setCurrentUserContext(userDietCtx);
//        SAPIConfiguration.setCurrentUser(vijay);
//
//        if (session["analyzed_texts"] == null) {
//            session["analyzed_texts"] = new ArrayList<TextAnalyzer>();
//        }
//    }
//
//    def afterInterceptor = {
//        SObjectImpl.getDataset().commit();
//        SObjectImpl.getDataset().end();
//    }

    def testCoreNLP() {
//        Properties props = new Properties();
//        // String modPath = "/scratch/WORK2/NL/stanford-corenlp/stanford-corenlp-full-2014-06-16/models3.4/edu/stanford/nlp/models/";
//
//        //String modPath = "C:\\Work\\StanfordNLP\\stanford-corenlp-full-2014-06-16\\models3.4\\edu\\stanford\\nlp\\models\\";
//        String modPath = "/Users/vkantharaj/workspace/aatma-me/ModelsFromJar/edu/stanford/nlp/models/";
//
//        props.put("pos.model", modPath + "pos-tagger/english-left3words/english-left3words-distsim.tagger");
//        //props.put("ner.model", modPath + "ner/english.all.3class.distsim.crf.ser.gz");
//        props.put("ner.model", modPath + "ner/english.all.3class.distsim.crf.ser.gz," + modPath + "ner/english.conll.4class.distsim.crf.ser.gz," + modPath + "ner/english.muc.7class.distsim.crf.ser.gz");
//        props.put("parse.model", modPath + "lexparser/englishPCFG.ser.gz");
//        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//        props.put("sutime.binders","0");
//        props.put("sutime.rules", modPath + "sutime/defs.sutime.txt," + modPath + "sutime/english.sutime.txt");
////    props.put("sutime.markTimeRanges", "true");
////    props.put("sutime.includeRange", "true");
////    props.put("sutime.includeNested", "true");
//        props.put("dcoref.demonym", modPath + "dcoref/demonyms.txt");
//        props.put("dcoref.states", modPath + "dcoref/state-abbreviations.txt");
//        props.put("dcoref.animate", modPath + "dcoref/animate.unigrams.txt");
//        props.put("dcoref.inanimate", modPath + "dcoref/inanimate.unigrams.txt");
//        //props.put("dcoref.big.gender.number", modPath + "dcoref/gender.data.gz");
//        props.put("dcoref.big.gender.number", modPath + "dcoref/gender.map.ser.gz");
//        props.put("dcoref.countries", modPath + "dcoref/countries");
//        props.put("dcoref.states.provinces", modPath + "dcoref/statesandprovinces");
//        props.put("dcoref.singleton.model", modPath + "dcoref/singleton.predictor.ser");

//    props.put("tokenize.options", "invertible,ptb3Escaping=true");
//    Properties propsTime = new Properties();
//    propsTime.setProperty("sutime.markTimeRanges", "true");
//    propsTime.setProperty("sutime.includeRange", "true");
//    TimeAnnotator sutime = new  TimeAnnotator("sutime", propsTime);

        // The following nset of annotators generates a different parse tree than one where
        // POS is used.
        // https://mailman.stanford.edu/pipermail/java-nlp-user/2011-August/001283.html
        // The above link explains the reason for the behavior.
        // But we use lemma and want to use dcoref (which depends on ner->lemma->POS
        // So we have to live with the parse, till Stanford folks fix it.
        // If we really want to get past this, we need to have two pipelines, and merge
        // the results ourselves, for lemma and dcoref.
        // props.put("annotators", "tokenize, ssplit, parse");


        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, pos, lemma, ner");
        def pipeline = new StanfordCoreNLP(props, false);
        System.out.println("Done loading");
        render ('done')
    }

    def index() {
        String userAboutNs = Constants.ABOX_PART + "User/About/" + "xyz123" + "#";
        Context userAboutCtx = new ContextImpl(userAboutNs);

        SIndividual vijay = new SIndividualImpl(userAboutNs + "VijayRaj", userAboutCtx);
        session["var"] = vijay;
        render "From session: " + session["var"]
    }

    def event() {
        log.info("Params event: " + params);
        if (params.processType == "Process") {
            log.info("One shot processing");
            session["intermediate_kbnl_graph"] = null;
            Article a = new Article((String)params.string);
            TextAnalyzer instance = new TextAnalyzer(a);
            instance.extractKnowledge();

            session["analyzed_texts"].add(0, instance);
            render (view: "show", model:['aText': instance])
        } else if (params.processType == "Process Step") {
            log.info("Two step processing");
            if (session["intermediate_kbnl_graph"] == null) {
                log.info("Step 1 of two step processing");
                redirect(controller: "event", action: "parseTextToGraph", params: params);
            } else {
                log.info("Step 2 of two step processing");
                redirect(controller: "event", action: "cleanGraph", params: params);
            }
        }
    }

    def parseTextToGraph () {
        // session["intermediate_kbnl_graph"] = null;
        log.info("Params parseTextToGraph: " + params);
        Article a = new Article(params.string);
        TextAnalyzer instance = new TextAnalyzer(a);
        instance.parseTextToKBNLGraphs();

        session["intermediate_kbnl_graph"] = instance;

        render (view: "show", model:['interimGraph':instance, "partAnalyzedText":params.string])
    }

    def cleanGraph () {
        log.info("Params cleanGraph: " + params);
        TextAnalyzer instance = session["intermediate_kbnl_graph"];
        if (instance == null) {
            log.info "Nothing intermediate graph to process!"
        }

        // Modify the KBNL Graph of the TextAnalyzer instance

        instance.cleanAndPersistGraph();
        session["analyzed_texts"].add(0, instance);

        session["intermediate_kbnl_graph"] = null;
        render (view: "show", model:['aText':instance])
    }


    def getNewConstantName() {
        // render "from controller: " + params.node_id;
        log.info("Params getNewConstantName: " + params);
        def indices = params.node_id.split(',')
        TextAnalyzer instance = session["intermediate_kbnl_graph"];
        BuildKBNLGraph kbNLGraph = instance.getKbNLGraphs().get(indices[0].toInteger());
        List<KBNLNodeCloud> nodes = kbNLGraph.getKbNLGraph().getKbNLNodes()
        KBNLNodeCloud kbNlNode = nodes.get(indices[1].toInteger());
        Tree nlParse = kbNlNode.getNlParse();

        String suggestedConstName = AnalyzePhrase.buildConstName(nlParse, kbNlNode.getClass());
        render suggestedConstName;
    }

    def modifyKBNLNode() {
        /*
         *     List<KBNLNodeCloud> nodes = instance.getKbNLGraphs().get(0).getKbNLGraph().getKbNLNodes();

        Set<RelationshipEdgeCloud> edges = instance.getKbNLGraphs().get(0).getKbNLGraph().getDg().edgeSet();
        edges.iterator().next().getFromNode().getSemanticPossibilities();

        log.info("KB NL Nodes available for edits: " + nodes);
        log.info("Get the berthing node: " + nodes.get(1));
        Tree nlParse = nodes.get(1).getNlParse();
        List<Object> newSemPos = AnalyzePhrase.parseVP(nlParse, true);
        for (Object aSemPos : newSemPos) {
          KBCollection eCol = (KBCollection) aSemPos;
          eCol.instantiates((KBCollection) FirstOrderCollectionImpl.getClassType(), Constants.uvMt());
          eCol.addGeneralization(EventImpl.getClassType(), Constants.uvMt());
        }
        // Morphology.lemmaStaticSynchronized(null, null, true)

        log.info("New semantic possibilities: " + newSemPos);
        nodes.get(1).setSemanticPossibilities(newSemPos);

        log.info("New KBNLGraph: " + instance.getKbNLGraphs().get(0).getKbNLGraph());
        */
        log.info("Params modifyKBNLNode: " + params);
        TextAnalyzer instance = session["intermediate_kbnl_graph"]
        def indices = params.node_id.split(',')
        List<KBNLNodeCloud> nodes = instance.getKbNLGraphs().get(indices[0].toInteger()).getKbNLGraph().getKbNLNodes()
        KBNLNodeCloud kbNlNode = nodes.get(indices[1].toInteger());
        Tree nlParse = kbNlNode.getNlParse();
        List<Object> newSemPos = AnalyzePhrase.parsePhrase(nlParse, kbNlNode.getClass());
        log.info("New semantic possibilities: " + newSemPos);
        kbNlNode.setSemanticPossibilities(newSemPos);
        render newSemPos.toString();
    }



    def assertSent() {
        log.info ("We got these params: " + params);
        List<Sentence> ruleOrSQueryPosLits = new ArrayList<Sentence>();
        List<Sentence> ruleOrSQueryNegLits = new ArrayList<Sentence>();
        SResultSet resSet = null;
        for (item in params.list("items")) {
            def indices = item.split(',')
            def aText = session["analyzed_texts"][indices[0].toInteger()];
            def aTextSentKE = aText.getPotentialKE().get(indices[1].toInteger());
            def aKEComb = aTextSentKE.getCombinations().get(indices[2].toInteger());
            Sentence aKESent = aKEComb.getSentences().get(indices[3].toInteger())
            ruleOrSQueryPosLits.add(aKESent);


            for (STerm t : aKESent.getListOfTypedVariables()) {
                ruleOrSQueryPosLits.add(t.getRestriction());
            }

            if (params.kbInteraction == "Assert") {
                List<STerm> terms = aKEComb.getTerms();
                List<STerm> reifiedTerms = new ArrayList<STerm>();
                for (t in terms) {
                    if (t instanceof SIndividual) {
                        reifiedTerms.add(((SIndividualImpl)t).reifyTypedVariable())
                    } else {
                        reifiedTerms.add(t)
                    }
                }
                log.info 'Reified terms: ' + reifiedTerms
                Sentence aKESentMod = aKESent.replaceTerms(terms, reifiedTerms);
                Context assertCtx = Constants.uvMt(); // This should be the user context
                // later assert in temporary user context, per conversation.
                // "ConversationalUserContext"
                // The user context should have a time stamp property
                // the context import hierarchy should have temporal links
                if (aKEComb.getContext()!= null) {
                    assertCtx = aKEComb.getContext();
                }
                Assertion sentAssertion = aKESentMod.assertIn(assertCtx)
                String parsedString = aTextSentKE.getBuildKBNLGraph().getSentenceString()
                log.info 'Parsed sentence: ' + parsedString;
//        FactImpl.findOrCreate(new SentenceImpl(KBPredicateImpl.get("creationSentence"), sentAssertion, parsedString), ContextImpl.get("BookkeepingMt"))
            }
        }

        for (negLit in params.list("negLits")) {
            def indices = negLit.split(',')
            def aText = session["analyzed_texts"][indices[0].toInteger()];
            def aTextSentKE = aText.getPotentialKE().get(indices[1].toInteger());
            def aKEComb = aTextSentKE.getCombinations().get(indices[2].toInteger());
            Sentence aKESent = aKEComb.getSentences().get(indices[3].toInteger())
            ruleOrSQueryNegLits.add(aKESent);
        }

        if (params.kbInteraction == "SQuery") {
            Sentence SQuerySent = SentenceImpl.and(ruleOrSQueryPosLits);
            log.info("SQuery sentence: " + SQuerySent);
            SQuery q = new SQueryImpl(SQuerySent, Constants.inferencePSCMt());
//      q.setInferenceMode(OpenCycInferenceMode.MINIMAL_MODE);
//      q.performInference();
//      resSet = q.getResultSet();
            resSet = q.execute()
            log.info("Results: " + resSet);

            render (view: "show", model:['resSet':resSet])
        } else {
            if (params.kbInteraction == "Assert SRule") {
                log.info("PosLits: " + ruleOrSQueryPosLits);
                log.info("NegLits: " + ruleOrSQueryNegLits);
                if (ruleOrSQueryNegLits.size() == 1) {
                    SRule r = SRuleImpl.findOrCreate(SentenceImpl.and(ruleOrSQueryPosLits), ruleOrSQueryNegLits.get(0), Constants.uvMt());
                } else {
                    SRule r = SRuleImpl.findOrCreate(SentenceImpl.and(ruleOrSQueryPosLits), SentenceImpl.and(ruleOrSQueryNegLits), Constants.uvMt());
                }
            }
            redirect (action: "show")
        }
    }

    def clearAnalyzedTextCache() {
        session["analyzed_texts"] = null;
        redirect (action: "show")
    }

    def clearAPICache() {
        // Jena has no cache to clear
//    KBObjectFactory.clearKBObjectCache();
        redirect (action: "show")
    }

    def show() {
        //redirect(action: "show", params: params)
        //[aText:params.aText]
    }
}
