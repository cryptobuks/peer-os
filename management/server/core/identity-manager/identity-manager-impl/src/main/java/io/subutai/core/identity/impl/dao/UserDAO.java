package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.impl.model.UserEntity;


/**
 * Implementation of User Dao Manager
 */
class UserDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( UserDAO.class );

    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    UserDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    User find( final long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        User result = null;
        try
        {
            daoManager.startTransaction( em );
            result = em.find( UserEntity.class, id );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    List<User> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<User> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select h from UserEntity h", User.class ).getResultList();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    void persist( User item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.persist( item );
            em.flush();

            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );

            throw new ActionFailedException( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    void remove( final Long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            UserEntity item = em.find( UserEntity.class, id );
            em.remove( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public void update( final User item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.merge( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( "Error updating user", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    User findByUsername( final String userName )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        User result = null;
        try
        {
            TypedQuery<UserEntity> query =
                    em.createQuery( "select u from UserEntity u where u.userName = :userName", UserEntity.class );
            query.setParameter( "userName", userName.toLowerCase() );

            List<UserEntity> users = query.getResultList();
            if ( !users.isEmpty() )
            {
                result = users.iterator().next();
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return result;
    }


    /* *************************************************
     *
     */
    User findByKeyId( final String keyId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        User result = null;
        try
        {
            TypedQuery<UserEntity> query =
                    em.createQuery( "select u from UserEntity u where u.securityKeyId = :keyId", UserEntity.class );
            query.setParameter( "keyId", keyId );

            List<UserEntity> users = query.getResultList();
            if ( !users.isEmpty() )
            {
                result = users.iterator().next();
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return result;
    }
}
