package controllers;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.util.PokeNames;
import okhttp3.OkHttpClient;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import views.html.cp;
import views.html.index;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by linleo on 8/10/2016.
 */
public class PokemonController extends Controller{

    @Inject
    FormFactory formFactory;

    private String REFRESHTOKEN = "refreshToken";

    public Result index() {

        String refreshToken = session(REFRESHTOKEN);

        if(refreshToken != null){
            return redirect("/pokemon/cp");
        }

        return ok(index.render("PokemonGo",GoogleUserCredentialProvider.LOGIN_URL,  ""));
    }

    public Result error() {

        return ok(index.render("PokemonGo",GoogleUserCredentialProvider.LOGIN_URL, "Something went wrong, please try again later"));
    }

    public Result logout() {

        String refreshToken = session(REFRESHTOKEN);

        if(refreshToken != null){
            session().remove(REFRESHTOKEN);
        }

        return redirect("/");
    }


    public Result cp(){

        Map<String, List<Pokemon>> pokemon = new HashMap<>();

        Locale localeTW = new Locale("zh", "TW");

        String refreshToken = session(REFRESHTOKEN);
        try {
            PokemonGo go = getGoApi(refreshToken);
            if(go != null){
                pokemon = listIvs(go,localeTW);
            }else{
                return redirect("/error");
            }
        }catch (Exception e){
            e.printStackTrace();
            session().remove(REFRESHTOKEN);
            return redirect("/error");
        }

        return ok(cp.render(pokemon));
    }

    private PokemonGo getGoApi(String refreshToken) throws Exception{
        PokemonGo go = null;
        if(refreshToken != null){
            OkHttpClient httpClient = new OkHttpClient();
            GoogleUserCredentialProvider provider;
            provider = new GoogleUserCredentialProvider(httpClient, refreshToken);

            go = new PokemonGo(provider, httpClient);
        }

        return go;
    }

    public Result auth() {
        DynamicForm bindedForm = formFactory.form().bindFromRequest();
        String token = bindedForm.get("token");

        try{
            OkHttpClient httpClient = new OkHttpClient();
            GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(httpClient);
            provider.login(token);
            String refreshToken = provider.getRefreshToken();
            session(REFRESHTOKEN, refreshToken);

        }catch (Exception e){
            e.printStackTrace();
        }

        return redirect("/pokemon/cp");
    }


    private Map<String, List<Pokemon>> listIvs(PokemonGo go, Locale localeTW) throws Exception{
        Inventories inventories = go.getInventories();

        List<Pokemon> pokemonList = inventories.getPokebank().getPokemons();

        Map<Integer, List<Pokemon>> pokemonMap = new HashMap<>();
        Map<String, List<Pokemon>> pokemonName = new LinkedHashMap<>();

        if(CollectionUtils.isNotEmpty(pokemonList)){
            for(Pokemon pokemon: pokemonList){

                int id = pokemon.getPokemonId().getNumber();

                if(pokemonMap.containsKey(id)){
                    pokemonMap.get(id).add(pokemon);
                }else{
                    List<Pokemon> pokemonLists = new ArrayList<Pokemon>();
                    pokemonLists.add(pokemon);
                    pokemonMap.put(id, pokemonLists);
                }
            }
        }

        PokemonComparator pokemonComparator = new PokemonComparator();

        Object[] idList = pokemonMap.keySet().toArray();

        Arrays.sort(idList);

        for(Object idObj: idList){

            Integer id = (Integer)idObj;
            String output = PokeNames.getDisplayName(id,localeTW );
            if(id == 29){
                output = "nidoranf";
            }else if(id == 32){
                output = "nidoranm";
            }

            List<Pokemon> pokemonSort = pokemonMap.get(id);
            Collections.sort(pokemonSort, pokemonComparator);
            pokemonName.put(output, pokemonSort);
        }

        return pokemonName;
    }

    public class PokemonComparator implements Comparator<Pokemon> {

        public int compare(Pokemon p1, Pokemon p2) {

            int result = 0;

            result = (int)(p2.getIvRatio()*100 - p1.getIvRatio()*100);
            if(result == 0){
                result = p2.getCp() - p1.getCp();
            }

            return result;

        }

    }
}
