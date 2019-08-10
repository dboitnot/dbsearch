package dbsearch;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * dbsearch: FormLoader
 * Created by dboitnot on 12/29/13.
 */
public class FormLoader {
    static FXMLLoader loaderForClass(Class cls) throws IOException {
        String path = "resources/" + cls.getSimpleName() + ".fxml";
        FXMLLoader loader = new FXMLLoader(cls.getResource(path));
        loader.load(cls.getResourceAsStream(path));
        return loader;
    }
}
