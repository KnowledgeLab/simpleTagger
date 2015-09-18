package klab;

import cc.mallet.fst.SimpleTagger;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.*;
import cc.mallet.util.CommandOption;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import cc.mallet.util.MalletLogger;
import junit.framework.TestCase;
import klab.*;
import klab.NewSimpleTaggerSentence2TokenSequence;
import klab.NewTokenTextCharSuffix;


/**
 * Created by Bartley on 9/9/15.
 */
public class PipeTest {

    public InstanceList trainingInstances;
    public InstanceList testingInstances;

    public static final String[] data = new String[] {
            "Free software is a matter of the users' freedom to run, copy, distribute, study, change and improve the software. More precisely, it refers to four kinds of freedom, for the users of the software.",
            "The freedom to run the program, for any purpose.",
            "The freedom to study how the program works, and adapt it to your needs.",
            "The freedom to redistribute copies so you can help your neighbor.",
            "The freedom to improve the program, and release your improvements to the public, so that the whole community benefits.",
            "A program is free software if users have all of these freedoms. Thus, you should be free to redistribute copies, either with or without modifications, either gratis or charging a fee for distribution, to anyone anywhere. Being free to do these things means (among other things) that you do not have to ask or pay for permission.",
            "You should also have the freedom to make modifications and use them privately in your own work or play, without even mentioning that they exist. If you do publish your changes, you should not be required to notify anyone in particular, or in any particular way.",
            "In order for the freedoms to make changes, and to publish improved versions, to be meaningful, you must have access to the source code of the program. Therefore, accessibility of source code is a necessary condition for free software.",
            "Finally, note that criteria such as those stated in this free software definition require careful thought for their interpretation. To decide whether a specific software license qualifies as a free software license, we judge it based on these criteria to determine whether it fits their spirit as well as the precise words. If a license includes unconscionable restrictions, we reject it, even if we did not anticipate the issue in these criteria. Sometimes a license requirement raises an issue that calls for extensive thought, including discussions with a lawyer, before we can decide if the requirement is acceptable. When we reach a conclusion about a new issue, we often update these criteria to make it easier to see why certain licenses do or don't qualify.",
            "In order for these freedoms to be real, they must be irrevocable as long as you do nothing wrong; if the developer of the software has the power to revoke the license, without your doing anything to give cause, the software is not free.",
            "However, certain kinds of rules about the manner of distributing free software are acceptable, when they don't conflict with the central freedoms. For example, copyleft (very simply stated) is the rule that when redistributing the program, you cannot add restrictions to deny other people the central freedoms. This rule does not conflict with the central freedoms; rather it protects them.",
            "Thus, you may have paid money to get copies of free software, or you may have obtained copies at no charge. But regardless of how you got your copies, you always have the freedom to copy and change the software, even to sell copies.",
            "Rules about how to package a modified version are acceptable, if they don't effectively block your freedom to release modified versions. Rules that ``if you make the program available in this way, you must make it available in that way also'' can be acceptable too, on the same condition. (Note that such a rule still leaves you the choice of whether to publish the program or not.) It is also acceptable for the license to require that, if you have distributed a modified version and a previous developer asks for a copy of it, you must send one.",
            "Sometimes government export control regulations and trade sanctions can constrain your freedom to distribute copies of programs internationally. Software developers do not have the power to eliminate or override these restrictions, but what they can and must do is refuse to impose them as conditions of use of the program. In this way, the restrictions will not affect activities and people outside the jurisdictions of these governments.",
            "Finally, note that criteria such as those stated in this free software definition require careful thought for their interpretation. To decide whether a specific software license qualifies as a free software license, we judge it based on these criteria to determine whether it fits their spirit as well as the precise words. If a license includes unconscionable restrictions, we reject it, even if we did not anticipate the issue in these criteria. Sometimes a license requirement raises an issue that calls for extensive thought, including discussions with a lawyer, before we can decide if the requirement is acceptable. When we reach a conclusion about a new issue, we often update these criteria to make it easier to see why certain licenses do or don't qualify.",
            "The GNU Project was launched in 1984 to develop a complete Unix-like operating system which is free software: the GNU system." };


    private static final CommandOption.Double gaussianVarianceOption = new CommandOption.Double
            (SimpleTagger.class, "gaussian-variance", "DECIMAL", true, 10.0,
                    "The gaussian prior variance used for training.", null);

    private static final CommandOption.Boolean trainOption = new CommandOption.Boolean
            (SimpleTagger.class, "train", "true|false", true, false,
                    "Whether to train", null);

    private static final CommandOption.String testOption = new CommandOption.String
            (SimpleTagger.class, "test", "lab or seg=start-1.continue-1,...,start-n.continue-n",
                    true, null,
                    "Test measuring labeling or segmentation (start-i, continue-i) accuracy", null);

    private static final CommandOption.File modelOption = new CommandOption.File
            (SimpleTagger.class, "model-file", "FILENAME", true, null,
                    "The filename for reading (train/run) or saving (train) the model.", null);

    private static final CommandOption.Double trainingFractionOption = new CommandOption.Double
            (SimpleTagger.class, "training-proportion", "DECIMAL", true, 0.5,
                    "Fraction of data to use for training in a random split.", null);

    private static final CommandOption.Integer randomSeedOption = new CommandOption.Integer
            (SimpleTagger.class, "random-seed", "INTEGER", true, 0,
                    "The random seed for randomly selecting a proportion of the instance list for training", null);

    private static final CommandOption.IntegerArray ordersOption = new CommandOption.IntegerArray
            (SimpleTagger.class, "orders", "COMMA-SEP-DECIMALS", true, new int[]{1},
                    "List of label Markov orders (main and backoff) ", null);

    private static final CommandOption.String forbiddenOption = new CommandOption.String(
            SimpleTagger.class, "forbidden", "REGEXP", true,
            "\\s", "label1,label2 transition forbidden if it matches this", null);

    private static final CommandOption.String allowedOption = new CommandOption.String(
            SimpleTagger.class, "allowed", "REGEXP", true,
            ".*", "label1,label2 transition allowed only if it matches this", null);

    private static final CommandOption.String defaultOption = new CommandOption.String(
            SimpleTagger.class, "default-label", "STRING", true, "O",
            "Label for initial context and uninteresting tokens", null);

    private static final CommandOption.Integer iterationsOption = new CommandOption.Integer(
            SimpleTagger.class, "iterations", "INTEGER", true, 500,
            "Number of training iterations", null);

    private static final CommandOption.Boolean viterbiOutputOption = new CommandOption.Boolean(
            SimpleTagger.class, "viterbi-output", "true|false", true, false,
            "Print Viterbi periodically during training", null);

    private static final CommandOption.Boolean connectedOption = new CommandOption.Boolean(
            SimpleTagger.class, "fully-connected", "true|false", true, true,
            "Include all allowed transitions, even those not in training data", null);

    private static final CommandOption.String weightsOption = new CommandOption.String(
            SimpleTagger.class, "weights", "sparse|some-dense|dense", true, "some-dense",
            "Use sparse, some dense (using a heuristic), or dense features on transitions.", null);

    private static final CommandOption.Boolean continueTrainingOption = new CommandOption.Boolean(
            SimpleTagger.class, "continue-training", "true|false", false, false,
            "Continue training from model specified by --model-file", null);

    private static final CommandOption.Integer nBestOption = new CommandOption.Integer(
            SimpleTagger.class, "n-best", "INTEGER", true, 1,
            "How many answers to output", null);

    private static final CommandOption.Integer cacheSizeOption = new CommandOption.Integer(
            SimpleTagger.class, "cache-size", "INTEGER", true, 100000,
            "How much state information to memoize in n-best decoding", null);

    private static final CommandOption.Boolean includeInputOption = new CommandOption.Boolean(
            SimpleTagger.class, "include-input", "true|false", true, false,
            "Whether to include the input features when printing decoding output", null);

    private static final CommandOption.Boolean featureInductionOption = new CommandOption.Boolean(
            SimpleTagger.class, "feature-induction", "true|false", true, false,
            "Whether to perform feature induction during training", null);

    private static final CommandOption.Integer numThreads = new CommandOption.Integer(
            SimpleTagger.class, "threads", "INTEGER", true, 1,
            "Number of threads to use for CRF training.", null);

    private static final CommandOption.Integer featureString = new CommandOption.Integer(
            SimpleTagger.class, "feature-string", "INTEGER", true, 156,
            "Specifies which features to look for in the input data", null);


    private static final CommandOption.List commandOptions =
            new CommandOption.List (
                    "Training, testing and running a generic tagger.",
                    new CommandOption[] {
                            gaussianVarianceOption,
                            trainOption,
                            iterationsOption,
                            testOption,
                            trainingFractionOption,
                            modelOption,
                            randomSeedOption,
                            ordersOption,
                            forbiddenOption,
                            allowedOption,
                            defaultOption,
                            viterbiOutputOption,
                            connectedOption,
                            weightsOption,
                            continueTrainingOption,
                            nBestOption,
                            cacheSizeOption,
                            includeInputOption,
                            featureInductionOption,
                            numThreads,
                            featureString
                    });



    public PipeTest(String trainingFilename, String testingFilename) throws IOException {

        ArrayList<Pipe> trPipes = new ArrayList<Pipe>();
        ArrayList<Pipe> tsPipes = new ArrayList<Pipe>();


        PrintWriter out = new PrintWriter(System.out);


        trPipes.add(new NewSimpleTaggerSentence2TokenSequence());
        tsPipes.add(new NewSimpleTaggerSentence2TokenSequence());
        //pipes.add(new FeaturesInWindow("PREV-", -1, 1));
        //pipes.add(new FeaturesInWindow("NEXT-", 1, 2));

        trPipes.add(new NewTokenTextCharSuffix("NUM=", 1));
        trPipes.add(new NewTokenTextCharSuffix("POS=", 5));
        trPipes.add(new NewTokenTextCharSuffix("VERB_PRE=", 1) );
        trPipes.add(new NewTokenTextCharSuffix("VERB_PAS=", 1));
        tsPipes.add(new NewTokenTextCharSuffix("NUM=", 1));
        tsPipes.add(new NewTokenTextCharSuffix("POS=", 5));

        tsPipes.add(new NewTokenTextCharSuffix("VERB_PRE=", 1) );
        tsPipes.add(new NewTokenTextCharSuffix("VERB_PAS=", 1));
        //pipes.add(new TokenTextCharSuffix("C2=", 2));
        //pipes.add(new TokenTextCharSuffix("C3=", 3));

        trPipes.add(new TokenSequence2FeatureVectorSequence());
        tsPipes.add(new TokenSequence2FeatureVectorSequence());
        //tsPipes.add(new NewSequencePrintingPipe(out));

        Pipe trpipe = new SerialPipes(trPipes);
        Pipe tspipe = new SerialPipes(tsPipes);

        trainingInstances = new InstanceList(trpipe);
        testingInstances = new InstanceList(tspipe);

        trainingInstances.addThruPipe(new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(trainingFilename))), Pattern.compile("^\\s*$"), true));
        testingInstances.addThruPipe(new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(testingFilename))), Pattern.compile("^\\s*$"), true));

        out.close();

    }

    public static void main (String[] args) throws Exception {
        System.out.println("HI");
        PipeTest trainer = new PipeTest(args[0], args[1]);

        InstanceList trainingData = trainer.trainingInstances;
        InstanceList testData = trainer.testingInstances;
        InstanceList validateData = null;

        //FeatureVectorSequence fvs = testData;
        Alphabet dict = testData.getAlphabet();
        //Alphabet dict = (fvs.size() > 0) ? fvs.getFeatureVector (0).getAlphabet () : null;
        System.out.println("HI");
        System.out.println(testData.size());
        System.err.println(trainingData.size());

        for (int i = 0; i < testData.size(); i++) {
            // writer.print (label);
            System.out.println("HI");

            FeatureVectorSequence fvs = (FeatureVectorSequence) testData.get(i).getData();
            for (int j = 0; j < fvs.size(); j++) {

                FeatureVector fv = fvs.get(j);
                System.err.println(' ');
                for (int loc = 0; loc < fv.numLocations(); loc++) {

                    String fname = dict.lookupObject(fv.indexAtLocation(loc)).toString();
                    double value = fv.valueAtLocation(loc);
                    //if (!Maths.almostEquals(value, 1.0)) {
                    //    throw new IllegalArgumentException ("Printing to SimpleTagger format: FeatureVector not binary at time slice "+i+" fv:"+fv);
                    //}
                    System.err.println(fname + String.valueOf(value));
                }

            }
            System.err.println();

        }

        /*
        Random r = new Random (0);
        InstanceList[] trainingLists =
                trainingData.split( r, new double[] {0.001, 0.999});
        trainingData = trainingLists[0];
        validateData = trainingLists[1];
        TransducerEvaluator[] eval = new TransducerEvaluator[3];



        boolean includeInput = true;

        CRF crf = null;

        crf = train(trainingData, testData, validateData, eval,
                ordersOption.value, defaultOption.value,
                forbiddenOption.value, allowedOption.value,
                connectedOption.value, iterationsOption.value,
                gaussianVarianceOption.value, crf);

        for (int l = 0; l< testData.size(); l++)
        {
            Sequence input = (Sequence)testData.get(l).getData();
            Sequence[] outputs = apply(crf, input, nBestOption.value);
            int k = outputs.length;
            boolean error = false;
            for (int a = 0; a < k; a++) {
                if (outputs[a].size() != input.size()) {
                    //logger.info("Failed to decode input sequence " + l + ", answer " + a);
                    error = true;
                }
            }
            if (!error) {
                for (int j = 0; j < input.size(); j++)
                {
                    StringBuffer buf = new StringBuffer();
                    for (int a = 0; a < k; a++)
                        buf.append(outputs[a].get(j).toString()).append(" ");
                    if (includeInput) {
                        FeatureVector fv = (FeatureVector)input.get(j);
                        buf.append(fv.toString(true));
                        //(FeatureVector)((FeatureVector) input.get(j)).value()
                    }
                    System.out.println(buf.toString());
                }
                System.out.println("");
            }
        }*/


    }


}
