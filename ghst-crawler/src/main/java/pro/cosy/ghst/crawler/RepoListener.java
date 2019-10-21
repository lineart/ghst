package pro.cosy.ghst.crawler;

import pro.cosy.ghst.data.Repo;

import java.util.List;


public interface RepoListener {

    void onNew(Repo repo);

    void onList(List<Repo> repoList);

}
