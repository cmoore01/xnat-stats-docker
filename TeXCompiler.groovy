import java.nio.file.Files
import java.nio.file.Path

public class TeXCompiler {

    public static List<Path> compileBarGraph(List data, int numBuckets, String graphName, String fileBase) {
        final Path tempDir = Files.createTempDirectory('tex_out')
        final String docClass = '\\documentclass[10 pt]{article}'
        final String packages = '\\usepackage{color}\n\\usepackage[dvipsnames]{xcolor}\n\\usepackage{pgfplots}\n\\usepackage{tikz}'
        final String begin = '\\begin{document}'
        final String end = '\\end{document}'

        final List<Bucket> buckets = Bucket.fixedBucketDivide(data, numBuckets)
        String graph = "\\centering\n\\begin{tikzpicture}\n"
        graph += "\t\\begin{axis}[symbolic x coords={${Bucket.join(buckets)}}, nodes near coords, xtick={${Bucket.join(buckets)}}, ymin=0, xtick pos=left, ytick pos=left, xticklabel style={rotate=45, major tick length=0pt}, title={${graphName.replace('_', '\\_')}}]\n"

        for (Bucket bucket in buckets) {
            graph += "\t\t\\addplot[ybar,fill=blue] coordinates {(${bucket.toString()}, ${bucket.size})};\n"
        }
        graph += '\t\\end{axis}\n\\end{tikzpicture}'
        final String tex = "${docClass}\n${packages}\n${begin}\n${graph}\n${end}"
        final Path texPath = tempDir.resolve("${fileBase}.tex")
        texPath.toFile().createNewFile()
        texPath.toFile() << tex

        def stdOut = new StringBuilder(), stdErr = new StringBuilder()
        def process = "pdflatex -output-directory=${tempDir.toAbsolutePath()} ${texPath.toAbsolutePath().toString()}".execute()
        process.consumeProcessOutput(stdOut, stdErr)
        process.waitForOrKill(10000)
        return [texPath, tempDir.resolve("${fileBase}.pdf")]
    }

}
